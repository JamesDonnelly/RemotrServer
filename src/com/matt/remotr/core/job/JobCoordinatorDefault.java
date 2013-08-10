package com.matt.remotr.core.job;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.event.Event;
import com.matt.remotr.core.event.EventCoordinator;
import com.matt.remotr.core.event.EventReceiver;
import com.matt.remotr.core.event.EventType;

public class JobCoordinatorDefault implements JobCoordinator, EventReceiver {
	
	private Logger log;
	private EventCoordinator eventCoordinator;
	private DeviceCoordinator deviceCoordinator;
	private Scheduler jobScheduler;
	
	private ArrayList<DetailTriggerHolder> jobList;
	
	public JobCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		log.info("Starting JobCoordinator - Now starting scheduler");
		try{
			jobScheduler = StdSchedulerFactory.getDefaultScheduler();
			jobScheduler.start();
			log.info("Scheduler started okay");
		}catch(SchedulerException se){
			log.error("Error starting the job scheduler.", se);
		}
		eventCoordinator.registerForEvents(this);
		jobList = new ArrayList<DetailTriggerHolder>();
	}

	public void setEventCoordinator(EventCoordinator eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}

	@Override
	public void registerManager(Device device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterManager(Device device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int createJob(Command command) {
		
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("Command", command);
		try {
			Device device = deviceCoordinator.getDeviceById(command.getDeviceId());
			jobDataMap.put("device", device);
		} catch (DeviceException e) {
			log.error("Device not found when creating job ["+e.getMessage()+"]");
		}
		
		JobDetail jobDetail = JobBuilder.newJob(CommandJob.class)
				.withIdentity(command.getName(), "CommandJob")
				.usingJobData(jobDataMap)
				.build();
		
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(command.getTrigger(), "CommandTrigger")
				.startNow()
				.build();
		
		DetailTriggerHolder dth = new DetailTriggerHolder(jobDetail, trigger);
		jobList.add(dth);
		return jobList.indexOf(dth);
		
	}

	@Override
	public int createJob(Command command, String cronExpression) {
		// TODO Auto-generated method stub
		return 0;
	}
  
	//TODO: Make this a custom exception
	//TODO: Add in the correct job listeners here. We are interested in a few things.
	@Override
	public void executeJob(int jobId) throws Exception {
		try{
			DetailTriggerHolder dth = jobList.get(jobId);
			log.info("Scheduling jobId ["+jobId+"]");
			jobScheduler.scheduleJob(dth.getJobDetail(), dth.getTrigger());
		}catch(IndexOutOfBoundsException e){
			log.error("JobId ["+jobId+"] not found on this coordinator");
			throw new Exception("Job not found");
		}catch(SchedulerException se){
			log.error("Error scheduling jobId ["+jobId+"]", se);
			throw new Exception(se);
		}
		
		// Create a broadcast event to say that this has been executed
		Event event = new Event();
		event.setName("JobId "+jobId+" is executing");
		event.setEventType(EventType.BROADCAST);
		
		eventCoordinator.forwardEvent(event, null);
	}

	@Override
	public void getJobStatus(int jobId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onBroadcastEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

}
