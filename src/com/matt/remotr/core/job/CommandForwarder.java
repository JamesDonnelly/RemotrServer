package com.matt.remotr.core.job;

import com.matt.remotr.core.device.Device;

/**
 * Allows implementing classes to forward jobs to devices
 * @author mattm
 *
 */
public interface CommandForwarder {
	
	/**
	 * Forwards the command to a device 
	 * @param device
	 * @param job
	 */
	public void forwardJob(Device device, RemotrJob job);

}
