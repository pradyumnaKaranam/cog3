package com.ibm.research.cogassist.db;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.QueueStatus;


public class UpdateOldestQueuedTaskWithInstanceIdQuery extends Update {

	public UpdateOldestQueuedTaskWithInstanceIdQuery(DataSource ds, int processorId) throws SQLException {
		super(ds, "db2".equalsIgnoreCase(CogAssist.getDBType())?
				"update catasks set processor_id = ? where id in (select id from "
				+ "catasks where status = ? and processor_id is null order "
				+ "by time_submitted asc fetch first 1 rows only)":
					"update catasks set processor_id = ? where status = ? "
					+ "and processor_id is null order by time_submitted asc limit 1");
		super.bind(processorId);
		super.bind(QueueStatus.QUEUED.toString());
	}

}
