package com.matt.remotr.core.job;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.core.device.Device;

public interface JobCoordinator {
	
	/**
	 * Registers the device with the JobCoordinator - This indicates to the Coordinator that that device is capable of running jobs. 
	 * @param device
	 */
	public void registerManager(Device device);
	
	/**
	 * Unregister the device with the JobCoordinator - This can be used to ensure no new jobs are sent to this device
	 * @param device
	 */
	public void unregisterManager(Device device);
	
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
	 * Executes the given job with the jobId and adds a listener to be notified on job events
	 * @param jobId
	 * @param jobEventListener
	 */
	//public void executeJob(int jobId, JobEventListener jobEventListener);
	
	/**
	 * Returns the status of the given jobId
	 * @param jobId
	 */
	public void getJobStatus(int jobId);
	

}
