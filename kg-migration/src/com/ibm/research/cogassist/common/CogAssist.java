package com.ibm.research.cogassist.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

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
		else return "C:\\Users\\IBM_ADMIN\\Desktop\\CognitiveAMS\\dbrefactor";
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

	/*Get Error Code : Error Message map for creating kg from db*/
	public static List<String> getErrorCodesMap(DataSource ds, String projectid) throws SQLException {
		return new SelectErrorMessagesMap(ds, projectid).execute();
	}
	
	/*
	 * Remove special characters except alphabets (lower upper, numerics)
	 * retain only , . _ - symbols
	 */
	public static String removeSpecialCharacters(String value){
		return value.replaceAll("[^a-zA-Z0-9/,._-]", " ");
	}
}
 
