package com.ibm.research.cogassist.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.QueueStatus;

public class UpdateRunningTasksToQueued extends Update {

	public UpdateRunningTasksToQueued(DataSource ds, int processorId) throws SQLException {
		super(ds, "update catasks set " +
				" status = ?, " +
				" last_update_time = ?, " +
				" time_started = NULL " +
				" where status = ? and processor_id = ?");
		super.bind(QueueStatus.QUEUED.toString());
		super.bind(new java.sql.Timestamp(new java.util.Date().getTime()));
		super.bind(QueueStatus.RUNNING.toString());
		super.bind(processorId);
	}

}
