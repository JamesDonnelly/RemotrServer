package com.matt.remotr.core.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.event.EventForwarder;

/**
 * An implementation of the Quartz Job interface for running commands that run on devices.
 * This is a wrapping for the {@link Command} class with some extra info
 * @author mattm
 *
 */
public class CommandJob implements Job {
	private EventForwarder eventForwarder;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		
		Command command = (Command) dataMap.get("command");
		Device device = (Device) dataMap.get("device");
		
	}

}
