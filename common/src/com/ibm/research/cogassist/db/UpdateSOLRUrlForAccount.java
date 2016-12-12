package com.ibm.research.cogassist.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
public class UpdateSOLRUrlForAccount extends Update {

	public UpdateSOLRUrlForAccount(DataSource ds, String projname, String url) throws SQLException {
		super(ds, "update project " +
				" set solr_search_url = ? " +
				" where id = ?"
				);
		super.bind(url);
		super.bind(CogAssist.getProjectID(ds,projname));		
	}
}
