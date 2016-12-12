package com.ibm.research.cogassist.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

class Constants
{
	//Attachment types
	public static final String FILE_DOC = "WORD_DOCUMENT";
	public static final String FILE_XLS = "EXCEL_SPREADSHEET";
	public static final String FILE_PPT = "POWERPOINT_PRESENTATION";
	public static final String FILE_PDF = "PDF_DOCUMENT";
	public static final String FILE_ZIP = "ZIP_DOCUMENT";	
	public static final String FILE_TXT = "TEXT_DOCUMENT";	
	public static final String FILE_UNKNOWN = "UNKNOWN";
	
	//File types
		public static final String FILE_EXTENSION_DOC = "DOC";
		public static final String FILE_EXTENSION_DOCX = "DOCX";
		public static final String FILE_EXTENSION_XLS = "XLS";
		public static final String FILE_EXTENSION_XLSX = "XLSX";
		public static final String FILE_EXTENSION_PPT = "PPT";
		public static final String FILE_EXTENSION_PPTX = "PPTX";
		public static final String FILE_EXTENSION_PDF = "PDF";
		public static final String FILE_EXTENSION_ZIP = "ZIP";
		public static final String FILE_EXTENSION_TXT = "TXT";
		public static final String FILE_EXTENSION_CSV = "CSV";
		
		
	//File type collections (used to identify the file type)
	public static List<String> WORD_FILE_LIST = new ArrayList<String>();
	static {
		List<String> wordDocList = new ArrayList<String>();
		wordDocList.add(FILE_EXTENSION_DOC);
		wordDocList.add(FILE_EXTENSION_DOCX);
		WORD_FILE_LIST = Collections.unmodifiableList(wordDocList);
	}

	public static List<String> EXCEL_FILE_LIST = new ArrayList<String>();
	static {
		List<String> excelFileList = new ArrayList<String>();
		excelFileList.add(FILE_EXTENSION_XLS);
		excelFileList.add(FILE_EXTENSION_XLSX);
		EXCEL_FILE_LIST = Collections.unmodifiableList(excelFileList);
	}
	
	public static List<String> PPT_FILE_LIST = new ArrayList<String>();
	static {
		List<String> pptFileList = new ArrayList<String>();
		pptFileList.add(FILE_EXTENSION_PPT);
		pptFileList.add(FILE_EXTENSION_PPTX);
		PPT_FILE_LIST = Collections.unmodifiableList(pptFileList);
	}
	
	public static List<String> TEXT_FILE_LIST = new ArrayList<String>();
	static {
		List<String> txtFileList = new ArrayList<String>();
		txtFileList.add(FILE_EXTENSION_TXT);
		txtFileList.add(FILE_EXTENSION_CSV);
		TEXT_FILE_LIST = Collections.unmodifiableList(txtFileList);
	}
}

public class Utils {
	
	public static synchronized List<File> getFilesFromFolder(File theFolder) {
		List<File> fileNamesList = new ArrayList<File>();
		if (theFolder.listFiles() == null) {
			return fileNamesList;
		}
		for (File fileEntry : theFolder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				fileNamesList.add(fileEntry);
			}
		}
		return fileNamesList;
	}
	
	public static synchronized List<File> getNestedFilesFromFolder(File theFolder) {
		List<File> fileNamesList = new ArrayList<File>();
		if (theFolder.listFiles() == null) {
			return fileNamesList;
		}
		for (File fileEntry : theFolder.listFiles()) {
			if (fileEntry.isDirectory()) {
				fileNamesList.addAll(getFilesFromFolder(fileEntry));
			} else {
				fileNamesList.add(fileEntry);
			}
		}
		return fileNamesList;
	}
	
	public static synchronized String getTextFromFile(String theFileName) throws Exception {
		String fileContent = null;
		File file = new File(theFileName);
		InputStreamReader isr = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
		BufferedReader theBufferedReader = new BufferedReader(isr);
		StringBuilder theStringBuilder = new StringBuilder();
        String line = theBufferedReader.readLine();
        while (line != null) {
            theStringBuilder.append(line);
            line = theBufferedReader.readLine();
        }
        fileContent = theStringBuilder.toString();
        theBufferedReader.close();
		return fileContent;
	}
	
	public static synchronized void writeStringToFile(String theOutputFolder, String theFileName, String theContent) throws Exception {
		//create the folder if it does not exist
		File theDestinationFolder = new File(theOutputFolder);
		if (!theDestinationFolder.exists()) {
			theDestinationFolder.mkdir();
		}

		File theDestinationFile = new File(theOutputFolder, theFileName);
		OutputStream theOutputStream = new FileOutputStream(theDestinationFile);

		byte[] bytes = theContent.getBytes();
		theOutputStream.write(bytes);
		theOutputStream.close();
	}
	
	public static synchronized void writeStringToFile(String theFileName, String theContent) throws Exception {
		File theDestinationFile = new File(theFileName);
		OutputStream outputStream = new FileOutputStream(theDestinationFile);
		Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, Charset.forName("UTF-8")));
		out.append(theContent);
		out.flush();
		out.close();
	}
	
	public static synchronized void writeStreamToFile(String theOutputFolder, String theFileName, InputStream theInputStream) throws Exception {
		//create the folder if it does not exist
		File theDestinationFolder = new File(theOutputFolder);
		if (!theDestinationFolder.exists()) {
			theDestinationFolder.mkdir();
		}
		
		//create the file
		File theDestinationFile = new File(theOutputFolder, theFileName);
		OutputStream theOutputStream = new FileOutputStream(theDestinationFile);

		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = theInputStream.read(bytes)) != -1) {
			theOutputStream.write(bytes, 0, read);
		}
		theOutputStream.close();
	}
	
	public static synchronized void copyFileUsingStream(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}
	
	public static synchronized String getFileNameWOExtension(String theFileName) throws Exception {
		int theExtensionDelimiterPosition = theFileName.lastIndexOf(".");

		if (theExtensionDelimiterPosition == -1) {
			return theFileName;
		}
		
		String theFileNameWOExtension = theFileName.substring(0, theExtensionDelimiterPosition).toUpperCase();
		return theFileNameWOExtension;
	}
	
	public static synchronized String getParentFilderName(String theFileName) throws Exception {
		int theExtensionDelimiterPosition = theFileName.lastIndexOf(".");

		if (theExtensionDelimiterPosition == -1) {
			return theFileName;
		}
		
		String theFileNameWOExtension = theFileName.substring(0, theFileName.length() - theExtensionDelimiterPosition).toUpperCase();
		return theFileNameWOExtension;
	}
	
	public static synchronized String getFileExtension(String theFileName) throws Exception {
		int theExtensionDelimiterPosition = theFileName.lastIndexOf(".");
		
		if (theExtensionDelimiterPosition == -1) {
			return "";
		}
		
		String fileExtension = theFileName.substring(theExtensionDelimiterPosition + 1, theFileName.length()).toUpperCase();
		return fileExtension;
	}
	
	public static synchronized String getFileType(String theFileName) throws Exception {
		int extensionDelimiterPosition = theFileName.lastIndexOf(".");
		
		if (extensionDelimiterPosition == -1) {
			return Constants.FILE_UNKNOWN;
		}
		
		String fileExtension = theFileName.substring(extensionDelimiterPosition + 1, theFileName.length()).toUpperCase();
		
		//check if it is a word document
		if (Constants.WORD_FILE_LIST.contains(fileExtension)) {
			return Constants.FILE_DOC;
		}

		//check if it is a text document
		if (Constants.TEXT_FILE_LIST.contains(fileExtension)) {
			return Constants.FILE_TXT;
		}

		//check if it is an excel spreadsheet
		if (Constants.EXCEL_FILE_LIST.contains(fileExtension)) {
			return Constants.FILE_XLS;
		}

		//check if it is a powerpoint presentation
		if (Constants.PPT_FILE_LIST.contains(fileExtension)) {
			return Constants.FILE_PPT;
		}

		//check if it is a pdf document
		if (Constants.FILE_EXTENSION_PDF.equalsIgnoreCase(fileExtension)) {
			return Constants.FILE_PDF;
		}

		//check if it is an archive
		if (Constants.FILE_EXTENSION_ZIP.equalsIgnoreCase(fileExtension)) {
			return Constants.FILE_ZIP;
		}

		//if extension is not among the known ones the unknown file type is returned
		return Constants.FILE_UNKNOWN;
	}

	public static String encodeURL(String url){
		url = url.replace("{", "%7B");
		url = url.replace("}", "%7D");
		url = url.replace(" ", "+");
		return url;
	}
	
	public static byte[] readBytesfromFile(String filename) throws IOException{
		File file = new File(filename);

		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("File is very large > " + Integer.MAX_VALUE);
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
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	}
	
	static public void writeFile(String filename, String output) {
		try {
			File outFile = new File(filename);
			FileWriter out = new FileWriter(outFile);
			out.write(output);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static int getNumEntriesInZip(String zipFile) throws ZipException, IOException 
	{          
		ZipFile zip = new ZipFile(new File(zipFile));         
		Enumeration zipFileEntries = zip.entries();       
		int i=0;
		while (zipFileEntries.hasMoreElements()) 
		{      
			zipFileEntries.nextElement(); 
			i++;
		}
		return i;
	}
	
	public static void zipDir(String dir2zip, ZipOutputStream zos , int start) 
	{ 
		try 
		{ 
	        //create a new File object based on the directory we have to zip 
	    	File  zipDir = new File(dir2zip); 
	        //get a listing of the directory content 
	        String[] dirList = zipDir.list(); 
	        byte[] readBuffer = new byte[2156]; 
	        int bytesIn = 0; 
	        //loop through dirList, and zip the files 
	        for(int i=0; i<dirList.length; i++) 
	        { 
	            File f = new File(zipDir, dirList[i]); 
		        if(f.isDirectory()) 
		        { 
		                //if the File object is a directory, call this 
		                //function again to add its content recursively 
		            String filePath = f.getPath(); 
		            zipDir(filePath, zos, start); 
		                //loop again 
		            continue; 
		        } 
		            //if we reached here, the File object f was not a directory 
		            //create a FileInputStream on top of f 
		        FileInputStream fis = new FileInputStream(f); 
		            //create a new zip entry 
		        ZipEntry anEntry = new ZipEntry(f.getPath().substring(start)); 
		            //place the zip entry in the ZipOutputStream object 
		        zos.putNextEntry(anEntry); 
		            //now write the content of the file to the ZipOutputStream 
	            while((bytesIn = fis.read(readBuffer)) != -1) 
	            { 
	                zos.write(readBuffer, 0, bytesIn); 
	            } 
	           //close the Stream 
	           fis.close(); 
		    } 
		} 
		catch(Exception e) 
		{ 
		    //handle exception 
		} 
	}
	
	public static String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {        
            return "";
        }
    }

	public static void writedToFile(InputStream is, File file) throws IOException {
		
/*		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		int c;
		while((c = is.read()) != -1) {
			out.writeByte(c);
		}
		is.close();
		out.close();
*/		
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			try {
			    out.write(convertStreamToString(is));
			} finally {
			    out.close();
			}
	}

	public static void writedToFile(String [] content, File file, boolean Append) throws IOException {
		
				Writer out = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(file,Append), "UTF-8"));
					try {
						for (int i=0;i<content.length;i++){
					    out.write(content[i]);
						}
					} finally {
					    out.close();
					}
	}
	
	public static void writedToFile(String content, File file, boolean Append) throws IOException {
		
		Writer out = new BufferedWriter(new OutputStreamWriter(
			    new FileOutputStream(file,Append), "UTF-8"));
			try {
				 out.write(content);
				}
			finally {
			    out.close();
			}
}
	
	static public String toString(Element elt) {
		String result = null;
		if (elt != null) {
			StringWriter strWtr = new StringWriter();
			StreamResult strResult = new StreamResult(strWtr);
			TransformerFactory tfac = TransformerFactory.newInstance();
			try {
				Transformer t = tfac.newTransformer();
				t.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
				// text
				// t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
				// "4");
				t.transform(new DOMSource(elt), strResult);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("XML.toString(Document): " + e);
			}
			result = strResult.getWriter().toString();
		}
		return result;
	}
	
	private static int CountLinesInaFile(String fileName) throws FileNotFoundException
	{
		LineNumberReader lineCounter = new LineNumberReader(
			new InputStreamReader(new FileInputStream(fileName)));
		String nextLine = null;
		try {
			while ((nextLine = lineCounter.readLine()) != null) {
				if (nextLine == null)
					break;
			}
		} 
		catch (Exception done) 
		{
			done.printStackTrace();
		}
		return lineCounter.getLineNumber();
	}

	public static String[] readAllLinesFromAFile1(String fileName) throws FileNotFoundException 
	{		
		String[] output = new String[CountLinesInaFile(fileName)];
		LineNumberReader lineCounter = new LineNumberReader(
		new InputStreamReader(new FileInputStream(fileName)));
		String nextLine = null;
		int i=0;
		try {
			while ((nextLine = lineCounter.readLine()) != null) {
				if (nextLine == null)
					break;
				//System.out.println("Server found "+nextLine);
				output[i]=nextLine;
				i++;
			}
			System.out.println("Tot number lines is "+i);
		} 
		catch (Exception done) {
			done.printStackTrace();
		}
		return output;
	}
	
	public static String readAllLinesFromAFile2(String fileName) throws FileNotFoundException 
	{		
		String output = "";
		LineNumberReader lineCounter = new LineNumberReader(
		new InputStreamReader(new FileInputStream(fileName)));
		String nextLine = null;
		int i=0;
		try {
			while ((nextLine = lineCounter.readLine()) != null) {
				if (nextLine == null)
					break;
				output += nextLine + "\n";
				i++;
			}
		} 
		catch (Exception done) {
			done.printStackTrace();
		}
		return output;
	}

	public static void ConvertFilesInAFolderInUTF8(){

	final File folderSource = new File("C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\DSTServerDataSept08\\DSTServerDataSept08");
	String folderDest = "C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\DSTServerDataSept08\\UTF-8";
	File[] listOfFiles = folderSource.listFiles();
	String[] rows;
	for (int i = 0; i < listOfFiles.length; i++) {
	File fileEntry = listOfFiles[i];
	if (!fileEntry.isDirectory()) {
	System.out.println(fileEntry.getName());
	try {
		rows = readAllLinesFromAFile1(folderSource+"\\"+fileEntry.getName());
		System.out.println(rows.length);
		writedToFile(rows, new File(folderDest+"\\"+fileEntry.getName()),false);
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	}
	}
	
}

	public static void AppendAllFilesInaFolder(){

//	final File folderSource = new File("C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\Roles");
//	String DestinationFilNameAndPath = "C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\Roles(Combined).csv";
//	final File folderSource = new File("C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\Members");
//	String DestinationFilNameAndPath = "C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\Members(Combined).csv";
//	final File folderSource = new File("C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\ProjectAreas");
//	String DestinationFilNameAndPath = "C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\ProjectAreas(Combined).csv";
	final File folderSource = new File("C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\Workitems_Txt");
	String DestinationFilNameAndPath = "C:\\Documents and Settings\\Administrator\\My Documents\\AISPractice\\GainAnalysis\\Workitems(Combined)Clean01.txt";
	
	File[] listOfFiles = folderSource.listFiles();
	for (int i = 0; i < listOfFiles.length; i++) {
		File fileEntry = listOfFiles[i];
		try {
			// create writer for file to append to
			BufferedWriter out = new BufferedWriter(
			new FileWriter(DestinationFilNameAndPath, true));
			// create reader for file to append from
			System.out.println("Processing File "+fileEntry.getName());
			BufferedReader in = new BufferedReader(new FileReader(folderSource+"\\"+fileEntry.getName()));
			String str;
			//The first two rows in each file contains headers
			str = in.readLine();
			str = in.readLine();
			while ((str = in.readLine()) != null) {
			//This condition is needed because the XML to CSV macro add extra rows...which are removed here
			if (!str.contains("																																					") )
					out.write(str);
			//else	System.out.println("clean row");
			out.write("\n");
			}
			in.close();
			out.close();
			 
			 
			} catch (IOException e) {
		
			}
	}
}

}
