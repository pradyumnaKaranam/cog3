package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;


public class SelectSearchUrlForAccount extends SingletonQuery<String> {
	
	public SelectSearchUrlForAccount(DataSource ds, String projname) throws SQLException {
		super(ds, "select solr_search_url from project where id = ?");
		super.bind(CogAssist.getProjectID(ds,projname));
	}

	public String getRow(ResultSet resultSet) throws SQLException {
		String url = resultSet.getString("solr_search_url");
		return url;
	}
}
