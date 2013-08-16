package com.matt.remotr.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.core.device.Device;
import com.matt.remotr.ws.response.WsJobResponse;

@WebService(targetNamespace="http://remotr.org/wsdl")
public interface JobCoordinatorWs {
	
	@WebMethod(operationName="CreateJob")
	public WsJobResponse createJob(@WebParam(name="Command") Command command);
	
	@WebMethod(operationName="CreateTimedJob")
	public WsJobResponse createTimedJob(@WebParam(name="Command") Command command, @WebParam(name="CronExpression") String cronExpression);
	
	@WebMethod(operationName="ExecuteJob")
	public WsJobResponse executeJob(@WebParam(name="JobId") int jobId);
	
	@WebMethod(operationName="ExecuteJobWithListener")
	public WsJobResponse executeJobWithListener(@WebParam(name="JobId") int jobId, @WebParam(name="Device") Device device);
	
	@WebMethod(operationName="GetJobDetails")
	public WsJobResponse getJobDetail(@WebParam(name="JobId") int jobId);
	
}
