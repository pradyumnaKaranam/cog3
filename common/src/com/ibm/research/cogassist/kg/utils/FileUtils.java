package com.ibm.research.cogassist.kg.utils;

/**
 * 
 * @author monikgup
 *
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {
	
	private static final String BINARY_MIME_TYPE = "application/octet-stream";
	
	private static final String[] CSV_MIME_TYPES = {
		    "text/csv",
		    "text/plain",
		    "application/csv",
		    "text/comma-separated-values",
		    //s"application/excel",
		    "application/vnd.ms-excel",
		    //"application/vnd.msexcel",
		    // "text/anytext",
		    // "application/txt"
		};
	
	private static final List<String> CSV_MIME_TYPES_LIST = Arrays.asList(CSV_MIME_TYPES);
	
	/** Returns whether the MIME content-type represents a CSV file. */
	public static boolean isCsvFile(String fileName, String contentType) {
		if (contentType.equalsIgnoreCase(BINARY_MIME_TYPE)) {
			return fileName.endsWith(".csv");
		} else {
			return CSV_MIME_TYPES_LIST.contains(contentType.toLowerCase());
		}
	}
		
	public static void copyfile(String srFile, String dtFile){
		try{
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			//For Append the file.
			//  OutputStream out = new FileOutputStream(f2,true);

			//For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

		}
		catch(FileNotFoundException ex){
			System.out.println(ex.getMessage() + " in the specified directory.");
		} catch (Exception e) {
			System.out.println("error in copyFile");
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	public static String[] readAllLinesFromFile(String filename) throws IOException{
		// Open the file
		FileInputStream fstream = new FileInputStream(filename);

		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine;
		List<String> strList = new ArrayList<String>();

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
			// Print the content on the console
			strList.add(strLine);
		}

		//Close the input stream
		in.close();
		return strList.toArray(new String[0]);

	}
	static public void writeFile(String filename, String output) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(output.getBytes("UTF-8"));
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static public String readFile(String fileName) throws IOException {
		String input = "";

		byte[] fileBytes = getBytesFromFile(new File(fileName));
		input = new String(fileBytes, "UTF8");
		return input;
	}

	// Returns the contents of the file in a byte array.
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	public static void cleanFile(String filename, String[] regex){
		String text;
		try {
			text = readFile(filename);
			for(String rgx : regex)
				text = text.replaceAll(rgx, "");
			writeFile(filename, text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		String folder = "E:/Ozone/crawl/SIH-full";
		File[] files = new File(folder).listFiles();
		for(File file : files){
			if(file.getAbsolutePath().endsWith(".xml")){
				String[] regex = {"&lt;b style=.*?&gt;","&lt;/b&gt;"};
				cleanFile(file.getAbsolutePath(), regex);
			}
		}
	}
}
