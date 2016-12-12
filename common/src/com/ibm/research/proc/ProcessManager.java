package com.ibm.research.proc;

import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A utility class for invoking native processes and
 * retriving their output.
 * 
 * @author Rohan Padhye
 *
 */
public class ProcessManager {
	
	private ProcessBuilder processBuilder = new ProcessBuilder();
	
	public Map<String, String> environment() {
		return processBuilder.environment();
	}
	
	public List<String> exec(String... command) throws NonZeroReturnValueException, IOException, InterruptedException {
		processBuilder.command(command);
		// processBuilder.redirectErrorStream(true);
		
		final Process process = processBuilder.start();		
		final List<String> output = new ArrayList<String>();
		
		Thread inputWriter = new Thread() {
			public void run () {
				try {
					process.getOutputStream().write(0);
					process.getOutputStream().close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
			}
		};
		
		Thread outputReader = new Thread() {
			public void run() {
				try {
					try {
						output.addAll(IOUtils.readLinesIgnoreCarriageReturn(process.getInputStream()));
					} finally {
						process.getInputStream().close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
			}
		};
		
		inputWriter.start();
		outputReader.start();
		
		
		if (process.waitFor() != 0) {
			throw new NonZeroReturnValueException(IOUtils.readLines(process.getErrorStream()), process.exitValue());
		}
		
		inputWriter.join();
		outputReader.join();
		
		return output;		
		
	}
	
	public void exec(final LineProcessor processor, String... command) throws NonZeroReturnValueException, IOException, InterruptedException {
		processBuilder.command(command);
		// processBuilder.redirectErrorStream(true);
		
		final Process process = processBuilder.start();		
		
		Thread inputWriter = new Thread() {
			public void run () {
				try {
					process.getOutputStream().write(0);
					process.getOutputStream().close();
				}  catch (IOException e) {
					throw new RuntimeException(e);
				} 
			}
		};
		
		Thread outputReader = new Thread() {
			public void run() {
				try {
					try {
						IOUtils.processLines(process.getInputStream(), processor);
					} finally {
						process.getInputStream().close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} 
			}
		};
		
		inputWriter.start();
		outputReader.start();
		
		
		if (process.waitFor() != 0) {
			throw new NonZeroReturnValueException(IOUtils.readLines(process.getErrorStream()), process.exitValue());
		}
		
		inputWriter.join();
		outputReader.join();
		
		
		if (process.waitFor() != 0) {
			throw new NonZeroReturnValueException(IOUtils.readLines(process.getErrorStream()), process.exitValue());
		}
				
		
	}
	
	/** Test entry point. */
	public static void main(String args[]) throws NonZeroReturnValueException, IOException, InterruptedException {
		List<String> lines = new ProcessManager().exec(args);
		for (String line : lines) {
			System.out.println(line);
		}
	}
}
