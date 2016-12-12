package com.ibm.research.cogassist.common;

import java.sql.SQLException;
import java.util.Date;


public class Task {
	private final Integer id;
	private final Integer projectId;
	private final String projectName;
	private final Domain domain;
	private final String owner;
	private QueueStatus status;
	private Date timeSubmitted;
	private Date timeStarted;
	private Date timeCompleted;
	private String info;
	private String language;
	
	public Task(Integer id, Integer projectId, Domain domain, String owner, QueueStatus status, Date timeSubmitted, Date timeStarted, Date timeCompleted, String info, String language) throws SQLException {
		this.id = id;
		this.projectId = projectId;
		this.projectName = CogAssist.getProjectName(projectId);
		this.owner = owner;
		this.domain = domain;
		this.status = status;
		this.timeSubmitted = timeSubmitted;
		this.timeStarted = timeStarted;
		this.timeCompleted = timeCompleted;
		this.info = info;
		this.language = language;
	}
	
	public Integer getId() {
		return id;
	}
	
	public Integer getProjectId() {
		return projectId;
	}
	
	public String getProjectName() {
		return projectName;
	}

	public Domain getDomain() {
		return domain;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public QueueStatus getQueueStatus() {
		return status;
	}
	
	public Date getSubmitTime() {
		return timeSubmitted;
	}
	
	public Date getStartTime() {
		return timeStarted;
	}
	
	public Date getCompleteTime() {
		return timeCompleted;
	}
	
	public QueueStatus getStatus() {
		return status;
	}
	
	public void setStatus(QueueStatus status) {
		this.status = status;
	}
	
	public String getInfo() {
		return info;
	}
	
	public String getLanguage() {
		return language;
	}
	
	@Override
	public String toString() {
		return getProjectName() + "#" + getId();
	}	
}
