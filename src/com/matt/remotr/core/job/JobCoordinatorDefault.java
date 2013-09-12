package com.matt.remotr.core.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.EventCoordinator;
import com.matt.remotr.core.event.EventReceiver;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.ResourceCoordinator;

/**
 * The default implementation of the {@link JobCoordinator}. This is responsible for creating and executing jobs, and creating JOB {@link EventType} to notify 
 * other devices and classes about {@link JobEvent}.
 * @author mattm
 *
 */

// TODO: Make job framework better - Clean up this code!
//TODO: Make jobs and their status persistent
public class JobCoordinatorDefault implements JobCoordinator, EventReceiver, JobListener {
	
	private Logger log;
	private EventCoordinator eventCoordinator;
	private DeviceCoordinator deviceCoordinator;
	private ResourceCoordinator resourceCoordinator;
	
	private Scheduler jobScheduler;
	private ArrayList<DetailTriggerHolder> jobList;
	private ConcurrentHashMap<Device, ArrayList<String>> deviceJobListenerMap;
	
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
		jobList = new ArrayList<DetailTriggerHolder>();
		deviceJobListenerMap = new ConcurrentHashMap<Device, ArrayList<String>>();
	}
	
	@PostConstruct
    public void init() {
		log.debug("Registering with eventCoordinator for events");
		eventCoordinator.registerForEvents(this);
    }

	public void setEventCoordinator(EventCoordinator eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}

	public void setDeviceCoordinator(DeviceCoordinator deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}
	
	public void setResourceCoordinator(ResourceCoordinator resourceCoordinator){
		this.resourceCoordinator = resourceCoordinator;
	}

	@Override
	public int createJob(Command command) {
		log.info("Creating job from command ["+command.getName()+"]");
		JobDataMap jobDataMap = new JobDataMap();
			
		jobDataMap.put("command", command);
		String jobName = command.getName() +"-"+ UUID.randomUUID();
		jobDataMap.put("jobname", jobName);
		jobDataMap.put("cronexpression", "");
		jobDataMap.put("jobstatus", JobStatus.CREATED);
		
		try {
			Device device = deviceCoordinator.getDeviceById(command.getDeviceId());
			jobDataMap.put("device", device);
		} catch (DeviceException e) {
			log.error("Device not found when creating job ["+e.getMessage()+"]");
		}
		
		jobDataMap.put("commandforwarder", eventCoordinator);
		
		log.debug("Adding jobDetail to CommandJob.class");
		JobDetail jobDetail = JobBuilder.newJob(CommandJob.class)
				.withIdentity(jobName, "CommandJob")
				.usingJobData(jobDataMap)
				.storeDurably(true) // Make sure we keep the job after the trigger is gone
				.build();
		
		log.debug("Adding trigger to job");
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(jobName, "CommandTrigger")
				.startNow()
				.build();
		
		DetailTriggerHolder dth = new DetailTriggerHolder(jobDetail, trigger);
		jobList.add(dth);
		int id = jobList.indexOf(dth);
		log.info("Created job with Id ["+id+"] - Returing");

		return id;
	}

	@Override
	public int createJob(Command command, String cronExpression) {
		// TODO Auto-generated method stub
		return 0;
	}
  
	//TODO: Make this a custom exception
	@Override
	public void executeJob(int jobId) throws Exception {
		executeJob(jobId, null);
	}

	@Override
	public void executeJob(int jobId, Device device) throws Exception{
		try{
			DetailTriggerHolder dth = jobList.get(jobId);
			log.info("Scheduling jobId ["+jobId+"]");
			dth.getJobDetail().getJobDataMap().put("jobstatus", JobStatus.SCHEDULED);
			
			if(device != null){
				if(!deviceJobListenerMap.containsKey(device)){
					ArrayList<String> jobNameList = new ArrayList<String>();
					deviceJobListenerMap.put(device, jobNameList);
				}
				String jobName = (String) dth.getJobDetail().getJobDataMap().get("jobname");
				deviceJobListenerMap.get(device).add(jobName);
			}
			
			jobScheduler.scheduleJob(dth.getJobDetail(), dth.getTrigger());
			jobScheduler.getListenerManager().addJobListener(this, jobKeyEquals(dth.getJobDetail().getKey()));
			
		}catch(IndexOutOfBoundsException e){
			log.error("JobId ["+jobId+"] not found on this coordinator");
			throw new Exception("Job not found");
		}catch(SchedulerException se){
			log.error("Error scheduling jobId ["+jobId+"]", se);
			throw new Exception(se);
		}
		
		// Create a broadcast event to say that this has been executed
		JobEvent jobEvent = new JobEvent();
		jobEvent.setName("JobNotification");
		jobEvent.setEventType(EventType.JOB);
		jobEvent.setJobStatus(JobStatus.CREATED);
		
		eventCoordinator.forwardEvent(jobEvent, null);
		
	}
	
	private Matcher<JobKey> jobKeyEquals(JobKey key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JobDataMap getJobDataMap(int jobId) throws Exception {
		log.info("Getting JobDataMap for jobId ["+jobId+"]");
		String jobName = (String) jobList.get(jobId).getJobDetail().getJobDataMap().get("jobname");
		JobDetail jd = jobScheduler.getJobDetail(getKeyFromJobName(jobName));
		if(jd == null){
			jd = jobList.get(jobId).getJobDetail();
		}
		return jd.getJobDataMap();
	}
	
	@Override
	public void onBroadcastEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(Event event) {
		if(event.getEventType() == EventType.JOB){
			log.debug("Recieved job event for job ["+event.getName()+"]");
			JobEvent jobEvent = (JobEvent) event;
			
			JobKey jk = getKeyFromJobName(jobEvent.getJobName());
			if(jk != null){
				try {
					JobDetail jobDetail = jobScheduler.getJobDetail(jk);
					if(jobDetail != null){
						log.info("Setting job ["+jobEvent.getJobName()+"] with JobStatus ["+jobEvent.getJobStatus().toString()+"]");
						JobDataMap jdm = jobDetail.getJobDataMap();
						jdm.put("jobstatus", jobEvent.getJobStatus());
						jobScheduler.addJob(jobDetail, true);
					}else{
						log.warn("Error getting JobDataMap - Has the job been removed?");
					}
				} catch (SchedulerException e) {
					log.error("Error getting job details", e);
				}
			}
		}
	}
	
	private JobKey getKeyFromJobName(String jobName){
		for(DetailTriggerHolder dht : jobList){
			JobKey jk = dht.getJobDetail().getKey();
			if(jk.getName().equals(jobName)){
				return jk;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "JobCoordinator";
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		// TODO Auto-generated method stub
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		log.debug("Job is about to be executed");	
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		log.info("Got JobExecuted context from scheduler. Building event");
		JobEvent jobEvent = new JobEvent();
		
		JobDataMap jdm = context.getJobDetail().getJobDataMap();
		
		jobEvent.setCommand((Command) jdm.get("command"));
		jobEvent.setJobName((String) jdm.get("jobname"));
		jobEvent.setJobStatus((JobStatus) jdm.get("jobstatus"));
		jobEvent.setEventType(EventType.JOB);
		jobEvent.setName("JobNotification");
		// TODO: jobEvent.setResource()
		
		log.debug("Getting devices interested in job ["+(String) jdm.get("jobname")+"]");
		
		log.info("Notifying listening devices and classes");
		Iterator it = deviceJobListenerMap.entrySet().iterator();
		Device systemDevice;
		try {
			systemDevice = deviceCoordinator.getSystemDevice();
			while(it.hasNext()){
				Map.Entry pairs = (Map.Entry) it.next();
				log.info("Notifying listening devices and classes");
				Device d = (Device) pairs.getKey();
				for(String as : (ArrayList<String>) pairs.getValue()){
					if(as.equals((String) jdm.get("jobname"))){
						eventCoordinator.forwardEvent(jobEvent, d);
					}
				}
			}
		} catch (DeviceException e) {
			log.error("Error getting system device - event will be forwarded", e);
		}
	}
	
}
