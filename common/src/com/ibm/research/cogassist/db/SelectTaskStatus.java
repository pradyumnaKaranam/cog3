package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.QueueStatus;

public class SelectTaskStatus extends SingletonQuery<Boolean> {
	public SelectTaskStatus(DataSource ds, String projectid, String domain) throws SQLException {
		super(ds, "select count(*) as count from catasks where project_id in (?) and status not in (?, ?) and domain = ?");
		super.bind(projectid);
		super.bind(QueueStatus.COMPLETED.toString());
		super.bind(QueueStatus.FAILED.toString());
		super.bind(domain);
	}
	
	@Override
	public Boolean getRow(ResultSet resultSet) throws SQLException {
		String count = resultSet.getString("count");
		if(count.equals("1"))
			return true;
		return false;
	}

}
