package com.matt.remotr.core.job;

import org.quartz.JobDataMap;

import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.device.domain.Device;

public interface JobCoordinator {
	
	/**
	 * Creates a job on the associated device for the given command. The job is created to run when received. 
	 * @param command
	 * @return
	 */
	public int createJob(Command command);
	
	/**
	 * Creates a job on the associated device for the given command. The job is created to run with given cron expression
	 * @param command
	 * @param cronExpression
	 * @return
	 */
	public int createJob(Command command, String cronExpression);
	
	/**
	 * Executes the given job with the jobId
	 * @param jobId
	 * @throws Exception 
	 */
	public void executeJob(int jobId) throws Exception;

	
	/**
	 * Executes the given job with the jobId and adds a device to send job events too for this job
	 * @param jobId
	 * @param device
	 * @throws Exception 
	 */
	public void executeJob(int jobId, Device device) throws Exception;
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws Exception
	 */
	public JobDataMap getJobDataMap(int jobId) throws Exception;
	

}
