package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.Domain;
import com.ibm.research.cogassist.common.QueueStatus;
import com.ibm.research.cogassist.common.Task;

public class SelectOldestQueuedTaskQuery extends SingletonQuery<Task> {

	public SelectOldestQueuedTaskQuery(DataSource ds, int processorId) throws SQLException {
		super(ds, "db2".equalsIgnoreCase(CogAssist.getDBType())?
				"select id, project_id, domain, user_id as task_user, time_submitted, time_started, time_completed, info, langsupport from catasks " +
				" where status = ? and processor_id = ? " +
				" order by time_submitted asc fetch first 1 rows only"
				:"select id, project_id, domain, user_id as task_user, time_submitted, time_started, time_completed, info, langsupport from catasks " +
				" where status = ? and processor_id = ? " +
				" order by time_submitted asc limit 1");
		super.bind(QueueStatus.QUEUED.toString());
		super.bind(processorId);
	}

	@Override
	public Task getRow(ResultSet resultSet) throws SQLException {
		Integer id = resultSet.getInt("id");
		Integer projectId = resultSet.getInt("project_id");
		Domain domain = Domain.valueOf(resultSet.getString("domain"));
		String owner = resultSet.getString("task_user");
		Date timeSubmitted = resultSet.getTimestamp("time_submitted");
		Date timeStarted = resultSet.getTimestamp("time_started");
		Date timeCompleted = resultSet.getTimestamp("time_completed");
		String info = resultSet.getString("info");
		String language = resultSet.getString("langsupport");
		return new Task(id, projectId, domain, owner, QueueStatus.QUEUED, timeSubmitted, timeStarted, timeCompleted, info, language);
	}
	

}
