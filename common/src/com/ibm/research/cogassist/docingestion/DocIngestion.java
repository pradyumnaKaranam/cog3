package com.ibm.research.cogassist.docingestion;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.docingestion.DocIngestionDomain;
public class DocIngestion {
	
	/**
	 * Folder Structure in Deployment 
	 * COGASSIST_HOME/home (inside this per account a folder to be created, and all documentse etc. should be handled there)
	 * COGASSIST_HOME/lib (all the 3rd part JARS will be present)
	 * COGASSIST_HOME/scripts (all the shell script and any other pdf2html script will be present)
	 * COGASSIST_HOME/plugins (all our code-jars will get in)
	 * COGASSIST_HOME/logs (all the logs will be created)
	 * 
	 */
	/**
	 * The name of the system property which contains the directory where Solr
	 * indexes are stored.
	 */
	public static final String SOLR_HOME_PROPERTY = "solr.solr.home";
	
	public static final String PUBLIC = "public";
	
	public static final String DOCUMENTS = "documents";


	/**
	 * SOLR URL name property
	 */
	public static final String SOLR_URL = "SOLR_URL";
	
	public static final String PDF_SCRIPT_LOCATION_PROPERTY = "PDF_SCRIPT_LOCATION";
	public static final String NODEAPP_LOCATION_PROPERTY = "NODEAPP_LOCATION";
	public static final String DB_SOLR_URL = "DB_SOLR_URL";
	/**
	 * SOLR URL name property
	 */
	public static final String CSV_FILE_NAME = "data.csv";

	/** The name of file which contains generic Document Ingestion properties. */
	public static final String PROPERTIES_FILE_NAME = "docingestion.properties";

	
	public static String getProjectLocation(String accountName) {
		return CogAssist.getHomeDirectoryName()  + File.separator + DocIngestionDomain.NAME + File.separator + accountName;
	}
	
	public static String getNodeAppLocation() throws IOException{
		return DocIngestion.getProperties().getProperty(DocIngestion.NODEAPP_LOCATION_PROPERTY);
	}
	
	public static String getNodeAppDocLocation(String accountName) throws IOException{
		return DocIngestion.getNodeAppLocation() + File.separator + PUBLIC + File.separator + DOCUMENTS +File.separator + accountName;
	}
	
	public static String getPDFScriptLocation()
	{
		return CogAssist.getHomeDirectoryName() + File.separator + ".." + File.separator + "scripts";
	}
	
	/** Returns the name of the cogassist-home directory. */
	public static String getSolrDirectoryName() {
		return System.getProperty(DocIngestion.SOLR_HOME_PROPERTY);
	}

	/** Get the directory in which global files are stored. */
	public static File getSharedDir() {
		return new File(CogAssist.getHomeDirectoryName());
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

	/** Returns the value of a generic application-wide property. */
	public static String getProperty(String key) throws IOException {
		return DocIngestion.getProperties().getProperty(key);
	}

	public static String getCsvDataPath(String accountName) {
		return CogAssist.getHomeDirectoryName() + File.separator + DocIngestionDomain.NAME + File.separator + accountName + File.separator + CSV_FILE_NAME;
	}
}
