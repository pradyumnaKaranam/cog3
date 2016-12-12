package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class SelectErrorCodeMessage extends SingletonQuery<String> {
	
	public SelectErrorCodeMessage(DataSource ds, String errorCode) throws SQLException {
		super(ds, "select error_code, error_msg from errorcode where error_code = ?");
		super.bind(errorCode);
	}

	@Override
	public String getRow(ResultSet resultSet) throws SQLException {
		String errorCode = resultSet.getString("error_code");
		String errorMessage = resultSet.getString("error_msg");
		return errorCode+":"+errorMessage;
	}
}
