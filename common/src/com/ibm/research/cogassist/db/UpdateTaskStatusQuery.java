package com.ibm.research.cogassist.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.QueueStatus;
import com.ibm.research.cogassist.common.Task;

public class UpdateTaskStatusQuery extends Update {

	public UpdateTaskStatusQuery(DataSource ds, Task task, QueueStatus status) throws SQLException {
		super(ds, "update catasks " +
				" set status = ?, " +
				startOrCompleteColumnName(status) + " = ?, " +
				" last_update_time = ?, " +
				" info = ? " +
				" where id = ?");
		super.bind(status.toString());
		super.bind(new java.sql.Timestamp(new java.util.Date().getTime()));
		super.bind(new java.sql.Timestamp(new java.util.Date().getTime()));
		super.bind(task.getInfo());
		super.bind(task.getId());		
	}
	
	private static String startOrCompleteColumnName(QueueStatus status) {
		switch (status) {
			case RUNNING:
				return "time_started";
			case COMPLETED:
			case FAILED:
			case ABORTED:
				return "time_completed";
			default:
				throw new IllegalArgumentException(status.toString());
		}
	}

}
