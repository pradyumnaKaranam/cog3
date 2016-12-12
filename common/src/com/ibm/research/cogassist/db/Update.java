package com.ibm.research.cogassist.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class Update extends Statement {
	
	public Update(DataSource ds, String sql, Object... params) {
		super(ds, sql, params);
	}
	
	public Integer execute() throws SQLException {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			stmt = prepareStatement(conn);
			stmt.executeUpdate();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				return null;
			}
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

}
