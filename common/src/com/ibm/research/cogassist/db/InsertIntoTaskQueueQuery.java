package com.ibm.research.cogassist.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.QueueStatus;

public class InsertIntoTaskQueueQuery extends Update {

	public InsertIntoTaskQueueQuery(DataSource ds, String project_id, String domainName, String user, String info, String language) throws SQLException {
		super(ds, "insert into catasks (project_id, domain, user_id, status, time_submitted, info, langsupport) " +
				" values (?, ?, ?, ?, ?, ?, ?)");
		
		super.bind(project_id);
		super.bind(domainName);
		super.bind(user);
		super.bind(QueueStatus.QUEUED.toString());
		super.bind(new java.sql.Timestamp(new java.util.Date().getTime()));
		super.bind(info);
		super.bind(language);
	}

}
