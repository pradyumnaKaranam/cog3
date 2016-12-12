package com.ibm.research.cogassist.common;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

public class DatabaseManager {

	/** A reference to the data source that will be used across the application. */
	private static DataSource dataSource = null;
	
	private static String dbType = null;

	/** Sets the data source. */
	public static void setDataSource(DataSource dataSource, String dbType) {
		DatabaseManager.dataSource = dataSource;
		DatabaseManager.dbType = dbType;
	}

	/** Returns the application-wide data source. 
	 * @throws IOException */
	public static DataSource getDataSource() throws IOException {
		if (dataSource == null){
			Properties dbCredentials = CogAssist.getDBProperties();
			dataSource = DatabaseManager.createJdbcDataSource(dbCredentials);
		}
		return dataSource;
	}
	
	/** Sets the data source. */
	public static void setdbType(String dbType) {
		DatabaseManager.dbType = dbType;
	}
	
	/** Returns the application-wide data source. */
	public static String getDbType() {
		return dbType;
	}
	
	/** Create a basic non-pooled data source with the given credentials as a JSON object.  */
	public static DataSource createJdbcDataSource(Properties credentials) {
		dbType 	= credentials.getProperty("DBTYPE");
		String hostName = credentials.getProperty("HOSTNAME");
		String hostPort = credentials.getProperty("HOSTPORT");
		String username = credentials.getProperty("USERNAME");
		String password = credentials.getProperty("PASSWORD");
		String database = credentials.getProperty("DATABASE");
		

		if(dbType.equalsIgnoreCase("mysql")){
			String jdbcConnectionString = "jdbc:mysql://" + hostName + ":" + hostPort + 
				                    "/" + database + 
									"?user=" + username + 
									"&password=" + password + 
									"&autoReconnect=true";
		
			return new JdbcDataSource(jdbcConnectionString, dbType);
		} else if(dbType.equalsIgnoreCase("db2")) {
			String jdbcConnectionString = "jdbc:db2://" + hostName + ":" + hostPort + 
                    "/" + database ;	
			return new JdbcDataSource(jdbcConnectionString, username, password, dbType);
			
			}
		return null;
		
	}
}
