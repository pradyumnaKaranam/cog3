package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class SelectProjectID extends SingletonQuery<String> {
	
	public SelectProjectID(DataSource ds, String projname) throws SQLException {
		super(ds, "select id from project where name = ?");
		super.bind(projname);
	}

	public String getRow(ResultSet resultSet) throws SQLException {
		String id = resultSet.getString("id");
		return id;
	}
}
