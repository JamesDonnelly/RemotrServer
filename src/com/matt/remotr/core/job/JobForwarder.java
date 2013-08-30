package com.matt.remotr.core.job;

import com.matt.remotr.core.device.Device;

/**
 * Allows implementing classes to forward jobs to devices
 * @author mattm
 *
 */
public interface JobForwarder {
	
	/**
	 * Forwards the job to a device 
	 * @param device - The Device to which to send the job
	 * @param job - The Job
	 */
	public void forwardJob(Device device, RemotrJob job);

}
