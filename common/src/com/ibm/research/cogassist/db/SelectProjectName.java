package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class SelectProjectName extends SingletonQuery<String> {
	
	public SelectProjectName(DataSource ds, Integer projectId) throws SQLException {
		super(ds, "select name from project where id = ?");
		super.bind(projectId);
	}

	@Override
	public String getRow(ResultSet resultSet) throws SQLException {
		return resultSet.getString("name");
	}

	

	

}
