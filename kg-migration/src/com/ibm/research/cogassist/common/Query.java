package com.ibm.research.cogassist.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

public abstract class Query<T> extends Statement {
	
	protected ResultSet resultSet;
	
	public Query(DataSource dataSource, String sql, Object... params) {
		super(dataSource, sql, params);
	}	
	
	public List<T> execute() throws SQLException {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		List<T> items = new LinkedList<T>();
		try {
			conn = dataSource.getConnection();
			stmt = prepareStatement(conn);
			rs = stmt.executeQuery();
			System.out.println("Result is "+rs);
			while(rs.next()) {
				T item = getRow(rs);
				items.add(item);
				
			}
			return items;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					
				}
			}
		}
	}
	
	public abstract T getRow(ResultSet resultSet) throws SQLException;
}
