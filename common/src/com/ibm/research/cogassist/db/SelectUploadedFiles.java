package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class SelectUploadedFiles extends Query<String[]> {

	public SelectUploadedFiles(DataSource ds, String projectid) throws SQLException {
		super(ds, "select filename, langsupport from documents where project_id in (?) and status in (?, ?)");
		super.bind(projectid);
		super.bind("UPLOADED");
		super.bind("REPLACED");
	}

	@Override
	public String[] getRow(ResultSet resultSet) throws SQLException {
		String[] array = new String[2];
		array[0] = resultSet.getString("filename");
		array[1] = resultSet.getString("langsupport");
		return array;
	}
}
