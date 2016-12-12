package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.json.JSONObject;

public class SelectTaskInfo extends Query<JSONObject> {
	public SelectTaskInfo(DataSource ds, String projectid, String domain) throws SQLException {
		super(ds, "db2".equalsIgnoreCase(CogAssist.getDBType())?
				"select * from catasks where project_id in (?) and domain = ? ORDER BY time_submitted DESC fetch first 5 rows only"
				:"select * from catasks where project_id in (?) and domain = ? ORDER BY time_submitted DESC LIMIT 5");
		System.out.println(projectid+":"+domain);
		super.bind(projectid);
		//super.bind(QueueStatus.RUNNING.toString());
		super.bind(domain);
	}
	
	@Override
	public JSONObject getRow(ResultSet resultSet) throws SQLException {
		JSONObject obj = new JSONObject();
		obj.put("id", resultSet.getInt("id"));
		obj.put("project_id", resultSet.getInt("project_id"));
		obj.put("domain", resultSet.getString("domain"));
		obj.put("user", resultSet.getString("user_id"));
		obj.put("status", resultSet.getString("status"));
		obj.put("time_submitted", resultSet.getString("time_submitted"));
		obj.put("time_started", resultSet.getString("time_started"));
		obj.put("time_completed", resultSet.getString("time_completed"));
		obj.put("info", resultSet.getString("info"));
		obj.put("processor_id", resultSet.getInt("processor_id"));
		return obj;
	}
}
