package com.ibm.research.cogassist.kg.creation;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.slf4j.Logger;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.Task;

public class KgCreationProcessor implements Callable<Void> {
private final Task kgCreationTask;
	
	private final Logger logger;
	
	public KgCreationProcessor(DataSource dataSource, Task task, Logger logger) throws SQLException {
		this.kgCreationTask = task;
		this.logger = logger;
	}
	
	@Override
	public Void call() throws Exception {
		// Get task-specific files and directories
		
		logger.info("Starting Preventive Ticket Analysis...");
		//String filePath = qprocessorHome+File.separator + kgCreationTask.getDomain() + File.separator+accountName+File.separator+kgCreationTask.getInfo();
		String filePath = CogAssist.getHomeDirectoryName()+File.separator+kgCreationTask.getDomain()+File.separator+kgCreationTask.getProjectId()+File.separator+"data.csv";
		
		KnowledgeGraphIngestor kgi = new KnowledgeGraphIngestor();
		if(kgCreationTask.getInfo().equalsIgnoreCase("DB")){
			logger.info("KG Creation is started from the DB");
			kgi.createKg(String.valueOf(kgCreationTask.getProjectId()), kgCreationTask.getLanguage());
			logger.info("KG Creation is completed from the DB");
		}else{
			logger.info("KG Creation is started from the File");
			kgi.createKg(filePath, String.valueOf(kgCreationTask.getProjectId()), kgCreationTask.getOwner(), kgCreationTask.getLanguage());
			logger.info("KG Creation is finished from the File");
		}
		// Launch preventive analysis engine
		//To Do
		logger.info("Knowledge Graph Creation is Completed.");

		
		
		
		return null;
	}
}
