package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class SelectErrorMessage extends SingletonQuery<String> {

	public SelectErrorMessage(DataSource ds, String errorCode) throws SQLException {
		super(ds, "select error_msg from errorcode where error_code = ?");
		super.bind(errorCode);
	}

	@Override
	public String getRow(ResultSet resultSet) throws SQLException {
		String errorMessage = resultSet.getString("error_msg");
		return errorMessage;
	}
}
