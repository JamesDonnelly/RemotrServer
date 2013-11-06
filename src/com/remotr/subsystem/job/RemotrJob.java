package com.remotr.subsystem.job;

import org.quartz.JobKey;

import com.remotr.subsystem.device.command.domain.Command;
import com.remotr.subsystem.job.domain.JobStatus;

/**
 * Base job class that all job classes should extend
 * @author mattm
 *
 */
public class RemotrJob {
	
	//protected int jobId;
	protected String jobName;
	protected JobStatus jobStatus;
	protected String cronExpression;
	protected Command command;
	protected JobKey jobKey;
	
//	public int getJobId() {
//		return jobId;
//	}
	
	public String getJobName() {
		return jobName;
	}
	
	public JobStatus getJobStatus() {
		return jobStatus;
	}
	
	public String getCronExpression() {
		return cronExpression;
	}
	
	public Command getCommand() {
		return command;
	}
	
	public JobKey getJobKey(){
		return jobKey;
	}

}
