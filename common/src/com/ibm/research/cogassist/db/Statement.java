package com.ibm.research.cogassist.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

public class Statement {
	
	protected DataSource dataSource;
	protected String sql;
	protected List<Object> params;
	
	protected Statement(DataSource dataSource, String sql, Object... params) {
		this.dataSource = dataSource;
		this.sql = sql;
		this.params = new ArrayList<Object>(Arrays.asList(params));
	}
	
	protected void bind(Object param) {
		params.add(param);
	}
	
	protected PreparedStatement prepareStatement(Connection db) throws SQLException {
		PreparedStatement stmt = null;
		stmt = db.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
		for(int i=0; i < params.size(); i++) {
			stmt.setObject(i+1, params.get(i));
		}
		return stmt;
	}
	
}
