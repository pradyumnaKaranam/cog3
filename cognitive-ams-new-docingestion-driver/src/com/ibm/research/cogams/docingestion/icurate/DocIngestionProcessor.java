package com.ibm.research.cogams.docingestion.icurate;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.slf4j.Logger;

import com.ibm.research.cogassist.common.Task;

public class DocIngestionProcessor implements Callable<Void> {

	private final Logger logger;
	private final Task docIngestionTask;

	public DocIngestionProcessor(DataSource dataSource, Task task, Logger logger)
			throws SQLException {
		this.docIngestionTask = task;
		this.logger = logger;
	}

	@Override
	public Void call() throws Exception {

		logger.info("Starting Document Ingestion...");
		Integer projectId = docIngestionTask.getProjectId();
		String projectName = docIngestionTask.getProjectName();
		String language = docIngestionTask.getLanguage();
		
		DocIngestionDriver.ingestDocs(projectId, projectName, language);
		
		//TODO Invoke the Document Chunking and Indexing Logic
		
		logger.info("Document Ingestion Complete.");

		return null;
	}

}
