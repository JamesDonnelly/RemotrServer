package com.matt.remotr.core.job;

import javax.annotation.PostConstruct;
import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.main.Main;
import com.matt.remotr.ws.JobCoordinatorWs;
import com.matt.remotr.ws.request.WsRequestManager;
import com.matt.remotr.ws.response.WsJobResponse;

@WebService(targetNamespace="http://remotr.org/wsdl", endpointInterface="com.matt.remotr.ws.JobCoordinatorWs", serviceName="job")
public class JobCoordinatorWsImpl extends SpringBeanAutowiringSupport implements JobCoordinatorWs {
	
	private Logger log;
	@Autowired
	private JobCoordinator jobCoordinator;
	@Autowired
	private WsRequestManager requestManager;
	
	public JobCoordinatorWsImpl(){
		log = Logger.getLogger(this.getClass());
		log.debug("Starting new JobCoordinator WebService");
	}
	
	@PostConstruct
	public void init(){
		requestManager.register(this);
	}

	@Override
	public WsJobResponse createJob(Command command) {
		log.info("Incoming request to create a new job from command");
		WsJobResponse jobResponse = getWsJobResponseForClass();
		
		int jobId = jobCoordinator.createJob(command);
		jobResponse.setJobId(jobId);
		jobResponse.setSuccess(true);
		
		return jobResponse;
	}

	@Override
	public WsJobResponse createTimedJob(Command command, String cronExpression) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public WsJobResponse executeJob(int jobId) {
		log.info("Incoming request to execute job ["+jobId+"]");
		WsJobResponse jobResponse = getWsJobResponseForClass();
		
		try {
			jobCoordinator.executeJob(jobId);
			jobResponse.setSuccess(true);
		} catch (Exception e) {
			log.error("Error executing job with Id ["+jobId+"]", e);
			jobResponse.setErrorMessage("Error executing job");
		}
		
		return jobResponse;
	}
	
	@Override
	public WsJobResponse executeJobWithListener(int jobId, Device device) {
		log.info("Incoming request to execute job ["+jobId+"]");
		WsJobResponse jobResponse = getWsJobResponseForClass();
		
		try {
			jobCoordinator.executeJob(jobId, device);
			jobResponse.setSuccess(true);
		} catch (Exception e) {
			log.error("Error executing job with Id ["+jobId+"]", e);
			jobResponse.setErrorMessage("Error executing job");
		}
		
		return jobResponse;
	}

	@Override
	public WsJobResponse getJobDetail(int jobId) {
		log.info("Incoming request to get job detail for ["+jobId+"]");
		WsJobResponse jobResponse = getWsJobResponseForClass();
		
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
	
	private WsJobResponse getWsJobResponseForClass(){
		WsJobResponse jobResponse = new WsJobResponse();
		jobResponse.setSubSystem(getSubSystemName());
		jobResponse.setVersionName(Main.getVersionName());
		jobResponse.setVersionNum(Main.getVersionNumber());
		return jobResponse;
	}

	@Override
	public String getSubSystemName() {
		return "JobCoordinator";
	}
	
}
