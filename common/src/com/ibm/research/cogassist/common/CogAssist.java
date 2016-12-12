package com.ibm.research.cogassist.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.ibm.research.cogassist.db.InsertIntoTaskQueueQuery;
import com.ibm.research.cogassist.db.SelectErrorCodeMessage;
import com.ibm.research.cogassist.db.SelectErrorMessage;
import com.ibm.research.cogassist.db.SelectErrorMessagesMap;
import com.ibm.research.cogassist.db.SelectOldestQueuedTaskQuery;
import com.ibm.research.cogassist.db.SelectProjectID;
import com.ibm.research.cogassist.db.SelectProjectName;
import com.ibm.research.cogassist.db.SelectSearchUrlForAccount;
import com.ibm.research.cogassist.db.UpdateDocumentStatusQuery;
import com.ibm.research.cogassist.db.UpdateOldestQueuedTaskWithInstanceIdQuery;
import com.ibm.research.cogassist.db.UpdateSOLRUrlForAccount;
import com.ibm.research.cogassist.docingestion.DocIngestionDomain;

public class CogAssist {

	/** Private constructor to force only static methods. */
	private CogAssist() {
		// Do nothing
	}

	

	/**
	 * The name of the system property which contains the name of the
	 * digdeep-home directory.
	 */
	public static final String HOME_PROPERTY = "COGASSIST_HOME";
	
	/** The name of the variable in Bluemix which has Digdeep DB parameters. */
	public static final String BLUEMIX_VCAP_SERVICES = "VCAP_SERVICES";
	
	/** The name of the digdeep database. */
	public static final String DATA_SOURCE_NAME = "jdbc/cogassist";
	
	/** The name of the digdeep database type. */
	public static final String DB_TYPE = "dbType";

	/** The name of file which contains generic DigDeep properties. */
	public static final String PROPERTIES_FILE_NAME = "cogassist.properties";

	/** The name of the file that contains the database credentials. */
	public static final String MYSQL_PROPERTIES_FILE_NAME = "db.properties";
	
	/**
	 * The name of the generic property which contains the hostname of the SMTP
	 * server used for sending emails.
	 */
	public static final String SMTP_HOST_PROPERTY = "SMTP_HOST";

	/**
	 * The name of the generic property which contains the port number of the
	 * SMTP server used for sending emails.
	 */
	public static final String SMTP_PORT_PROPERTY = "SMTP_PORT";

	/**
	 * The name of the generic property which contains the email address from
	 * which mails are sent.
	 */
	public static final String EMAIL_ADDRESS = "EMAIL_ADDRESS";
	
	public static final String UPLOADS_FOLDER = "files";
	
	/** Returns the name of the digdeep-home directory. */
	public static String getHomeDirectoryName() {
		if( System.getProperty(CogAssist.HOME_PROPERTY)!= null)
			return System.getProperty(CogAssist.HOME_PROPERTY);
		else return "/Users/sampath/cogassist/home";
	}
	
	// A cache of the generic application-wide properties object. */
	private static Properties properties;

	/** Returns the generic application-wide properties object. */
	public static Properties getProperties() throws IOException {
		if (properties == null) {
			File propertiesFile = new File(getSharedDir(), PROPERTIES_FILE_NAME);
			properties = new Properties();
			properties.load(new FileReader(propertiesFile));
		}
		return properties;
	}
	
	/** Returns the generic application-wide properties object. */
	public static Properties getDomainProperties(String domainName) throws IOException {
		File propertiesFile = new File(getSharedDir(), domainName+".properties");
		Properties domainProperties = new Properties();
		domainProperties.load(new FileReader(propertiesFile));
		return domainProperties;
	}

	/** Returns the value of a generic application-wide property. */
	public static String getProperty(String key) throws IOException {
		return CogAssist.getProperties().getProperty(key);
	}

	/** Get the directory in which an account's files can be stored. */
	public static File getAccountDirectory(String accountName) {
		File dir = new File(CogAssist.getHomeDirectoryName(), accountName);
		if (!dir.exists()) {
			dir.mkdir();
		}
		return dir;
	}

	/** Get the directory in which global files are stored. */
	public static File getSharedDir() {
		return new File(CogAssist.getHomeDirectoryName());
	}
	
	/** Returns the MySQL credentials read from a properties file. */
	public static Properties getDBProperties() throws IOException {
		File propertiesFile = new File(getSharedDir(),
				MYSQL_PROPERTIES_FILE_NAME);
		Properties properties = new Properties();
		properties.load(new FileReader(propertiesFile));
		return properties;
	}

	/** Gets the oldest unprocessed task in the queue owned by some processor. */
	public static Task getOldestQueuedTask(DataSource ds, int processorId) throws SQLException {
		// First atomically get ownership of a queued task then return it.
		new UpdateOldestQueuedTaskWithInstanceIdQuery(ds, processorId).execute();
		// The following should select what we just updated or null if nothing was updated.
		return new SelectOldestQueuedTaskQuery(ds, processorId).fetch();	
	}
	
	/** Enqueues a task in the database. */
	public static void enqueueTask(DataSource ds, String accountName,
			String domainName, String owner, String info, String language) throws SQLException {
		new InsertIntoTaskQueueQuery(ds, accountName, domainName, owner, info, language).execute();
	}

	/*Return the Error Message given the Error Code.*/
	public static String getErrorMessage(DataSource ds, String errorCode, Boolean append ) throws SQLException {
		if(!append)
			return new SelectErrorMessage(ds, errorCode).fetch();
		return new SelectErrorCodeMessage(ds, errorCode).fetch();
	}
	
	/*Get Error Code : Error Message map for creating kg from db*/
	public static List<String> getErrorCodesMap(DataSource ds, String projectid) throws SQLException {
		return new SelectErrorMessagesMap(ds, projectid).execute();
	}
	
	/*Get Project ID*/
	public static String getProjectID(DataSource ds, String projname) throws SQLException {
		return new SelectProjectID(ds, projname).fetch();
	}
	
	/*Update Document Status*/
	public static void updateDocumentStatus(DataSource ds, String projname, String status, String docname, String reason) throws SQLException {
		new UpdateDocumentStatusQuery(ds, projname,status,docname,reason).execute();
	}
	public static void updateDocumentStatus(DataSource ds, String projname, String status, String docname) throws SQLException {
		new UpdateDocumentStatusQuery(ds, projname,status,docname).execute();
	}
	
	/*Update Search URL*/
	public static void updateSearchUrlForAccount(DataSource ds, String projname, String url) throws SQLException {
		new UpdateSOLRUrlForAccount(ds, projname,url).execute();
	}
	
	public static String checkSearchUrlForAccount(DataSource ds, String projname) throws SQLException {
		return new SelectSearchUrlForAccount(ds,projname).fetch();
	}

	/*Get the datatype from database manager.*/
	public static String getDBType(){
		return DatabaseManager.getDbType();
	}
	/*Get the project name given the projectid*/
	public static String getProjectName(Integer projectId) throws SQLException {
		DataSource ds;
		try {
			ds = DatabaseManager.getDataSource();
			return new SelectProjectName(ds,projectId).fetch();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/** Returns the name of the directory containing file dump to be processed. */
	public static String getDumpLocationName(String accountName, String language) {
		return CogAssist.getHomeDirectoryName()  + File.separator + DocIngestionDomain.NAME + File.separator + accountName + File.separator+ language + File.separator + UPLOADS_FOLDER ;
	}
	
	/*
	 * Remove special characters except alphabets (lower upper, numerics)
	 * retain only , . _ - symbols
	 */
	public static String removeSpecialCharacters(String value){
		return value.replaceAll("[^a-zA-Z0-9/,._-]", " ");
	}
}
 
