package com.ibm.research.cogassist.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.Domain;
import com.ibm.research.cogassist.common.QueueStatus;
import com.ibm.research.cogassist.common.Task;



public class SelectTaskByIdQuery extends SingletonQuery<Task> {

	
	public SelectTaskByIdQuery(DataSource ds, Integer taskId) throws SQLException {
		super(ds, "select id, project_id, domain, user_id as task_user, status, time_submitted, time_started, time_completed, info, langsupport " +
				" from catasks ct where id = ?");
		super.bind(taskId);
	}

	public Task getRow(ResultSet resultSet) throws SQLException {
		Integer taskId = resultSet.getInt("id");
		Integer projectId = resultSet.getInt("project_id");
		Domain domain = Domain.valueOf(resultSet.getString("domain"));
		String owner = resultSet.getString("task_user");
		QueueStatus status = QueueStatus.valueOf(resultSet.getString("status"));
		Date timeSubmitted = resultSet.getTimestamp("time_submitted");
		Date timeStarted = resultSet.getTimestamp("time_started");
		Date timeCompleted = resultSet.getTimestamp("time_completed");
		String info = resultSet.getString("info");
		String language = resultSet.getString("langsupport");

		return new Task(taskId, projectId, domain, owner, status, timeSubmitted, timeStarted, timeCompleted, info, language);
	}
	
	

}
