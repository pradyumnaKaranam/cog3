package com.ibm.research.cogassist.utils;

import java.io.PrintWriter;

/**
 * A utility for writing CSVs with proper string escaping.
 *
 */
public class CsvWriter {
	/** The number of columns in the output CSV. */
	private Integer numColumns;
	
	/** The output stream to where the CSV should be written. */
	private final PrintWriter writer;
	
	/** The separator used for separating column values. */
	private String SEPARATOR = ",";
	
	/** Creates a new CSV writer that will write to some output stream. */
	public CsvWriter(PrintWriter writer) {
		this.writer = writer;
	}
	
	/** Set and write the headers. */
	public void headers(String... headers) {
		// Ensure only one set of headers is used
		if (numColumns != null) {
			throw new IllegalStateException("Column headers have already been set.");
		}
		
		// Remember the column length
		this.numColumns = headers.length;
		
		// Write the headers
		writer.println(arrayToColumns((Object[]) headers));
	}
	
	/** Writes one line of data to the CSV. */
	public void write(Object... args) throws ArrayIndexOutOfBoundsException {
		// Ensure headers have been set
		if (numColumns == null) {
			throw new IllegalStateException("Column headers have not yet been set.");
		}
		
		// Check number of columns
		if (args.length != numColumns) {
			throw new ArrayIndexOutOfBoundsException("CSV must contain " + numColumns + " columns since there are as many headers.");
		}
		
		// Write values
		writer.println(arrayToColumns(args));
	}
	
	private String arrayToColumns(Object... args) {
		StringBuffer lineBuf = new StringBuffer();
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			StringBuffer valBuf = new StringBuffer();
			if (arg instanceof Iterable) {
				Iterable<?> elems = (Iterable<?>) arg;
				boolean nonEmpty = false;
				for (Object elem : elems) {
					valBuf.append(elem);
					valBuf.append(this.SEPARATOR);
					nonEmpty = true;
				}
				if (nonEmpty) {
					valBuf.delete(valBuf.length()-this.SEPARATOR.length(), valBuf.length());
				}
			} else if (arg != null) {
				valBuf.append(arg.toString());
			}
			lineBuf.append(encloseValue(valBuf.toString()));
			if (i < args.length - 1) {
				lineBuf.append(this.SEPARATOR);
			}
		}
		return lineBuf.toString();
	}
	
	/** Returns a column value enclosed in quotes with internal quotes escaped as double. */
	public static String encloseValue(String str) {
		return "\"" + str.replace("\"", "\"\"") + "\"";
	}
	
	/** Closes the output-stream used by this CSV writer. */
	public void close() {
		writer.close();
	}
}
