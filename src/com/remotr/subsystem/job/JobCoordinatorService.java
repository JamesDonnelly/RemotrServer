package com.remotr.subsystem.job;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;

import com.remotr.core.Main;
import com.remotr.device.command.domain.Command;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.job.domain.JobStatus;
import com.remotr.subsystem.ws.WsBase;
import com.remotr.subsystem.ws.WsClass;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsMethod;
import com.remotr.subsystem.ws.WsParam;
import com.remotr.subsystem.ws.WsRunner;
import com.remotr.subsystem.ws.response.domain.WsJobResponse;

@WsClass(description = "Handles job triggering and reporting")
public class JobCoordinatorService extends WsBase implements WsRunner {
	
	private Logger log;
	private JobCoordinator jobCoordinator;
	private WsCoordinator wsCoordinator;
	
	public JobCoordinatorService(){
		log = Logger.getLogger(this.getClass());
		subSystemName = "Job";
		log.debug("Starting new JobCoordinator WebService");
	}
	
	@PostConstruct
	public void init(){
		wsCoordinator.register(this);
	}

	@WsMethod(
			isPublic=false,
			description="Creates a job",
			wsParams = { 
					@WsParam(name="command", type=Command.class)
			})
	public WsJobResponse createJob(Command command) {
		log.info("Incoming request to create a new job from command");
		WsJobResponse jobResponse = getWsResponseForClass();
		
		int jobId = jobCoordinator.createJob(command);
		jobResponse.setJobId(jobId);
		jobResponse.setSuccess(true);
		
		return jobResponse;
	}

	public WsJobResponse createTimedJob(Command command, String cronExpression) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@WsMethod(
			isPublic=false,
			description="Executes the given job Id",
			wsParams = { 
					@WsParam(name="jobId", type=Integer.class)
			})
	public WsJobResponse executeJob(int jobId) {
		log.info("Incoming request to execute job ["+jobId+"]");
		WsJobResponse jobResponse = getWsResponseForClass();
		
		try {
			jobCoordinator.executeJob(jobId);
			jobResponse.setSuccess(true);
		} catch (Exception e) {
			log.error("Error executing job with Id ["+jobId+"]", e);
			jobResponse.setErrorMessage("Error executing job");
		}
		
		return jobResponse;
	}
	
	@WsMethod(
			isPublic=false,
			isAsync=true,
			description="Executes the given job Id and attaches the given device as a listener. "
					+ "This device will recieve job status updates",
			wsParams = { 
					@WsParam(name="jobId", type=Integer.class),
					@WsParam(name="device", type=Device.class)
			})
	public WsJobResponse executeJobWithListener(int jobId, Device device) {
		log.info("Incoming request to execute job ["+jobId+"]");
		WsJobResponse jobResponse = getWsResponseForClass();
		
		try {
			jobCoordinator.executeJob(jobId, device);
			jobResponse.setSuccess(true);
		} catch (Exception e) {
			log.error("Error executing job with Id ["+jobId+"]", e);
			jobResponse.setErrorMessage("Error executing job");
		}
		
		return jobResponse;
	}

	@WsMethod(
			isPublic=false,
			description="Gets the details for the given job",
			wsParams = { 
					@WsParam(name="jobId", type=Integer.class)
			})
	public WsJobResponse getJobDetail(int jobId) {
		log.info("Incoming request to get job detail for ["+jobId+"]");
		WsJobResponse jobResponse = getWsResponseForClass();
		
		try {
			JobDataMap jdm = jobCoordinator.getJobDataMap(jobId);
			jobResponse.setCommand((Command) jdm.get("command"));
			jobResponse.setJobId(jobId);
			jobResponse.setJobName((String) jdm.get("jobname"));
			jobResponse.setJobStatus((JobStatus) jdm.get("jobstatus"));
			jobResponse.setSuccess(true);
		} catch (Exception e) {
			log.error("Error getting job status for job ["+jobId+"]", e);
			jobResponse.setErrorMessage("Error executing job");
		}
		
		return jobResponse;
	}
	
	@Override
	protected WsJobResponse getWsResponseForClass(){
		WsJobResponse jobResponse = new WsJobResponse();
		jobResponse.setSubSystem(getSubSystemName());
		jobResponse.setVersionName(Main.getVersionName());
		jobResponse.setVersionNum(Main.getVersionNumber());
		return jobResponse;
	}

	@Override
	public String getSubSystemName() {
		return subSystemName;
	}

	public void setJobCoordinator(JobCoordinator jobCoordinator) {
		this.jobCoordinator = jobCoordinator;
	}

	public void setWsCoordinator(WsCoordinator wsCoordinator) {
		this.wsCoordinator = wsCoordinator;
	}
	
}
