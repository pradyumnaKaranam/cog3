package com.ibm.research.cogassist.common;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class JdbcDataSource implements DataSource {
	
	protected String url;
	protected String userName;
	protected String password;
protected String dbType;
	
	public JdbcDataSource(String url, String dbType) {
		this.dbType = dbType;
		this.url = url;
	}
	
	
	public JdbcDataSource(String url, String userName, String password, String dbType) {
		this.dbType = dbType;
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		try {
			if(dbType.equalsIgnoreCase("mysql"))
				Class.forName("com.mysql.jdbc.Driver");
			else if(dbType.equalsIgnoreCase("db2"))
				Class.forName("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}	
		
		
		if(userName == null && password == null)
			return DriverManager.getConnection(url);
		return DriverManager.getConnection(url, userName, password);
	}


	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return new PrintWriter(System.out);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new UnsupportedOperationException();
	}

}
