package com.matt.remotr.core.job;

import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.core.device.Device;

/**
 * An implementation of the Quartz Job interface for running commands that run on devices.
 * This is a wrapping for the {@link Command} class with some extra info
 * @author mattm
 *
 */
@PersistJobDataAfterExecution
public class CommandJob extends RemotrJob implements Job {
	
	private Logger log;
	private JobDataMap dataMap;
	private Device device;
	private JobForwarder commandForwarder;
	
	private void init(JobExecutionContext context){
		log = Logger.getLogger(this.getClass());
		
		dataMap = context.getJobDetail().getJobDataMap();
		
		jobKey = context.getJobDetail().getKey();
		command = (Command) dataMap.get("command");
		jobName = (String) dataMap.get("jobname");
		jobStatus = (JobStatus) dataMap.get("jobstatus");
		cronExpression = (String) dataMap.get("cronexpression");
		
		device = (Device) dataMap.get("device");
		commandForwarder = (JobForwarder) dataMap.get("commandforwarder");
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		init(context);
		
		log.debug("JobKey = ["+jobKey.toString()+"]");		
		log.debug("Got device ["+device.getName()+"] for job");
		log.debug("Got command ["+command.getName()+"] for job");
		log.info("Starting job ["+jobName+"]");
		
		commandForwarder.forwardJob(device, this);
		jobStatus = JobStatus.EXECUTING;
		dataMap.put("jobstatus", jobStatus);
	}

}
