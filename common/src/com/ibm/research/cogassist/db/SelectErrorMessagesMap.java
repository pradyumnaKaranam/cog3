package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class SelectErrorMessagesMap extends Query<String> {

	public SelectErrorMessagesMap(DataSource ds, String projectid) throws SQLException {
		super(ds, "select error_code, error_msg from errorcode where project_id in (?)");
		super.bind(projectid);
	}

	@Override
	public String getRow(ResultSet resultSet) throws SQLException {
		return resultSet.getString("error_code")+":"+resultSet.getString("error_msg");
		
	}
}