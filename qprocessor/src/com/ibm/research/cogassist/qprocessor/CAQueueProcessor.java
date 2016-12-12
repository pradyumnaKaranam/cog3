package com.ibm.research.cogassist.qprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.DatabaseManager;
import com.ibm.research.cogassist.common.QueueStatus;
import com.ibm.research.cogassist.common.Task;
import com.ibm.research.cogassist.db.UpdateRunningTasksToQueued;
import com.ibm.research.cogassist.db.UpdateTaskStatusQuery;
import com.ibm.research.cogassist.qprocessor.util.Email;
import com.ibm.research.cogassist.qprocessor.util.JarDirectoryClassLoader;

/**
 * A driver and processor which picks up items from the task
 * queue and dispatches task processors depending on the domain.
 *
 */
public class CAQueueProcessor {
	
	/** Logger. */
	private final Logger logger;
	
	/** The instance ID of this queue processor. */
	private final Integer id;
	
	/** The single database connection which is used to manage pending and completed tasks. */
	protected final DataSource ds;

	/** The directory in which plug-in JARs as well as the domain mapping is stored. */
	private final File pluginDirectory;
	
	/** The directory in which task logs are stored. */
	private final File logsDirectory;
	
	/** The output stream used by the Queue Processor. */
	private PrintStream queueLogStream;
	
	/** Whether the queue processor should be running. */
	private boolean running;
	
	/** A constant containing the time (in milliseconds) between each poll of the task queue, in case it is empty. */
	public static final long POLL_INTERVAL = 5000;
	
	/** Creates a new queue processor with a given database connection and resource directories. */
	public CAQueueProcessor(Integer id, DataSource ds, File processorDirectory, File logsDirectory) {
		this.id = id;
		this.ds = ds;
		this.pluginDirectory = processorDirectory;		
		this.logsDirectory = logsDirectory;
		this.logger = LoggerFactory.getLogger(CAQueueProcessor.class + " (" + id + ")");
	}
	
	/** Returns the instance ID of this queue processor. */
	public Integer getId() {
		return this.id;
	}
	
	/** Gets the next task to process if the queue is non-empty or else blocks until something comes into the queue.  */
	synchronized protected Task getNextTask() {		
		Task task = null;
		do {
			try {
				Thread.sleep(POLL_INTERVAL);
				logger.debug("Attempting to fetch next task [{}]", new Date());
				task = CogAssist.getOldestQueuedTask(ds, this.id);
			} catch (InterruptedException e) {
				logger.error("Thread interrupted: " + e.getMessage());
			} catch (SQLException e) {
				logger.error("Could not fetch task: " + e.getMessage());
			}
		} while (task == null);
		logger.info("Fetched task: {}", task);
		return task;
		
	}
	
	/** Returns the callable task processor appropriate for the domain of the given task.     */
	@SuppressWarnings("unchecked")
	protected Callable<Void> getTaskProcessor(Task task, ClassLoader pluginLoader, Logger taskLogger) throws Exception {
		// Get the task processor for the current task's domain
		String processorClassName = task.getDomain().getTaskProcessorClassName();
		
		// Load the task processor class
		Class<Callable<Void>> processorClass = (Class<Callable<Void>>) pluginLoader.loadClass(processorClassName);
		
		// Get the constructor for this class which takes a data source and task ID as input
		Constructor<Callable<Void>> processorConstructor = processorClass.getDeclaredConstructor(DataSource.class, Task.class, Logger.class);
		
		// Create an instance by invoking the constructor
		Callable<Void> processorInstance = processorConstructor.newInstance(ds, task, taskLogger);
		
		// Return this instance
		return processorInstance;
	}

	/** Processes tasks in a loop. */
	synchronized public void process() {
		logger.info("Starting Queue Processor main loop.");
		running = true;
		while(running) {
			try {
				// First set all RUNNING tasks to QUEUED (to recover from crashes)
				new UpdateRunningTasksToQueued(ds, this.id).execute();
				// Now pick queued tasks and run
				while (running) {
					// Get next task from the queue
					Task task = getNextTask();
					
					// Create a logger for this task
					Logger taskLogger = LoggerFactory.getLogger(task.toString());
					
					// Set up a stream to log output for this task
					PrintStream taskLogStream = null;
					
					// Set up a class-loader to dynamically load plugins
					JarDirectoryClassLoader pluginLoader = null;					
					
					try {			
						// Fix an output stream for logging purposes
						try {
							taskLogStream = new PrintStream(new FileOutputStream(new File(logsDirectory, task.getId() + ".log")));
						} catch (FileNotFoundException e) {
							logger.error("Could not open log file for task: " + e.getMessage());
							taskLogStream = queueLogStream;
						}		
						
						// Create a temporary class-loader for dynamic loading of plugin classes
						pluginLoader = new JarDirectoryClassLoader(pluginDirectory);
						
						// Load the task processor
						Callable<Void> taskProcessor = getTaskProcessor(task, pluginLoader, taskLogger);
						
						// Mark the start of the task
						setTaskStatus(task, QueueStatus.RUNNING);

						// Execute the task processor
						execute(taskProcessor, logger, taskLogStream);
						
						// Mark the end of the task
						setTaskStatus(task, QueueStatus.COMPLETED);
						
					} catch (InterruptedException e) { 
						stop();						
					} catch (Throwable e) {
						// Mark the failure of the task
						setTaskStatus(task, QueueStatus.FAILED);

						taskLogStream.println(e);
						e.printStackTrace(taskLogStream);
						
						logger.error(e.getClass().getName() + ": " + e.getMessage());
						
					} finally {
						// Release resources opened by the class-loader
						if (pluginLoader != null) {
							try {
								pluginLoader.close();
							} catch (Exception ignore) { }
						}
						
						// If the task log output was different, close it.
						if (taskLogStream !=null && taskLogStream != queueLogStream ) {
							taskLogStream.close();
						}
						
						// Log fate of the task 
						logger.info("Task {} ended with status: {}", task, task.getStatus());
					}
				}
			} catch (SQLException e) {
				logger.error("Main process loop crashed: " + e.getMessage());
			}
		}
		logger.info("Exiting Queue Processor main loop.");
		
	}
	
	/** Executes a task with output streams redirect to the given log streams.  */
	protected synchronized void execute(Callable<?> processor, Logger logger, PrintStream logStream) throws Exception {
		// Save the old streams
		PrintStream out = System.out;
		PrintStream err = System.err;
		
		// Redirect all output to the log streams
		System.setOut(logStream);
		System.setErr(logStream);
		
		// Execute the task
		try {
			// Call the processor
			processor.call();
			
			// If the thread was interrupted from outside, then abort.
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
		} catch(InterruptedException e) {
			throw e;
		} catch (Exception e) { 
			logger.error("Task failed!", e);
			throw e;
		}
		finally {
			// Whether the call succeeded or not, we MUST reset the output sreams
			System.setOut(out);
			System.setErr(err);
		}
	}
	
	/** Signals the QP to exit the main loop. */
	public void stop() {
		running = false;
	}
	
	
	/** Sets the status of a task. Used by processors when they complete or fail.  */
	synchronized protected void setTaskStatus(Task task, QueueStatus status) throws SQLException {
		// Update task status in database
		new UpdateTaskStatusQuery(ds, task, status).execute();
		// Change the object state
		task.setStatus(status);
		// Send an email to all admins of the project
		try {
			// List<String> admins = new SelectAdminUsersForAccountQuery(ds, task.getAccountName()).execute();
			// for (String admin : admins) {
			String admin = task.getOwner();
				String subject, message;
				switch (status) {
					case COMPLETED:
						subject = task.getDomain()+": Analysis Completed Successfully";
						message = String.format("*** THIS IS AN AUTOMATICALLY GENERATED EMAIL BY THE COGNITIVE ASSISTANT SYSTEM ***\n\n" + 
								 "An analysis task with ID %d " +
								 "for the account [%s] has completed successfully " +
								 "on %s. ", 
								 task.getId(), task.getProjectName(), new Date());
								 //Util.getDigDeepUrl() + "/view.jsp?accountName="+task.getAccountName());
						break;
					case FAILED:
	
						subject = task.getDomain()+": Analysis Failed";
						message = String.format("*** THIS IS AN AUTOMATICALLY GENERATED EMAIL BY THE COGNITIVE ASSISTANT SYSTEM ***\n\n" + 
								 "An analysis task with ID %d " +
								 "for the account [%s] has ended with failure " +
								 " at %s. Please contact the DigDeep system administrators " +
								 "to find out why this happened and how the issue can be resolved."
								 + task.getId(), task.getProjectName(), new Date());
						break;
					case ABORTED:
						subject = task.getDomain()+": Analysis Aborted";
						message = String.format("*** THIS IS AN AUTOMATICALLY GENERATED EMAIL BY THE COGNITIVE ASSISTANT SYSTEM ***\n\n" + 
								 "An analysis task with ID %d " +
								 "for the account [%s] has been prematurely aborted " +
								 "on %s. ", 
								 task.getId(), task.getProjectName(), new Date());
						break;
					default:
						return; // No email for other status changes (such as re-runs)
				}
				// Send email
				Email.sendEmail(CogAssist.getProperty(CogAssist.SMTP_HOST_PROPERTY), 
						        CogAssist.getProperty(CogAssist.SMTP_PORT_PROPERTY), 
						        null, null, CogAssist.getProperty(CogAssist.EMAIL_ADDRESS), 
						        admin, subject, message);
			// }
		} catch(RuntimeException e) { 
			logger.error("Could not send email. ", e);
		} catch (Exception e) {
			logger.error("Could not send email: " + e.getMessage());
		}
		
	}

	/** Connects to the database and invokes the queue processor. */
	public static void main(String args[]) throws IOException {
		
		if (args.length < 3) {
			System.err.println("Usage: " + CAQueueProcessor.class.getName() + " INSTANCE_ID PLUGIN_DIRECTORY LOGS_DIRECTORY");
			System.exit(1);
		}
		
		Integer instanceId = null;
		try {
			instanceId = new Integer(args[0]);
		} catch (NumberFormatException ignore) { }
		
		if (instanceId == null || instanceId <= 0) {
			System.err.println("Instance ID must be a natural number.");
			System.exit(1);
		}
		
		// Database connection
		
		// Database connection
		Properties dbCredentials = CogAssist.getDBProperties();
		DataSource dataSource = DatabaseManager.createJdbcDataSource(dbCredentials);
		DatabaseManager.setDataSource(dataSource, dbCredentials.getProperty("DBTYPE"));
		
		
		// Build a queue processor with the created data source
		CAQueueProcessor q = new CAQueueProcessor(instanceId, dataSource, new File(args[1]), new File(args[2]));
		q.process();
		
	}
	
}
