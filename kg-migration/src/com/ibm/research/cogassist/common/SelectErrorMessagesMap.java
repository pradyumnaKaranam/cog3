package com.ibm.research.cogassist.common;

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
		System.out.println(resultSet.getString("error_code")+":"+resultSet.getString("error_msg"));
		return resultSet.getString("error_code")+":"+resultSet.getString("error_msg");
		
	}
}
