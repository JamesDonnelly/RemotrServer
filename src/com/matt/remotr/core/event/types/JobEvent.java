package com.matt.remotr.core.event.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.quartz.JobKey;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.core.job.JobStatus;

@XmlRootElement
public class JobEvent extends Event {
	
	private int jobId;
	private String jobName;
	private JobStatus jobStatus;
	private JobKey jobKey;
	private String cronExpression;
	private Command command;
	
	@XmlElement(name="JobId")
	public int getJobId() {
		return jobId;
	}
	
	@XmlElement(name="JobName")
	public String getJobName() {
		return jobName;
	}
	
	@XmlElement(name="JobStatus")
	public JobStatus getJobStatus() {
		return jobStatus;
	}
	
	@XmlElement(name="CronExpression")
	public String getCronExpression() {
		return cronExpression;
	}
	
	@XmlElement(name="Command")
	public Command getCommand() {
		return command;
	}
	
	@XmlElement(name="JobKey")
	public JobKey getJobKey() {
		return jobKey;
	}

	public void setJobKey(JobKey jobKey) {
		this.jobKey = jobKey;
	}
	
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}
	
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
	public void setCommand(Command command) {
		this.command = command;
	}

}
