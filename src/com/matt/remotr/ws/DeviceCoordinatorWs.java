package com.matt.remotr.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.device.domain.DeviceType;
import com.matt.remotr.ws.request.WsRequestRunner;
import com.matt.remotr.ws.response.domain.WsDeviceResponse;

@WebService(targetNamespace="http://remotr.org/wsdl")
public interface DeviceCoordinatorWs extends WsRequestRunner {
	
	@WebMethod(operationName="RegisterDevice")
	public WsDeviceResponse register(@WebParam(name="Device") Device device);
	
	@WebMethod(operationName="DeregisterDevice")
	public WsDeviceResponse deregister(@WebParam(name="Device") Device device);
	
	@WebMethod(operationName="CheckRegistered")
	public WsDeviceResponse checkRegistered(@WebParam(name="Name") String name, @WebParam(name="Type") DeviceType type);
	
	@WebMethod(operationName="GetDeviceById")
	public WsDeviceResponse getDeviceById(@WebParam(name="deviceId") int id);
	
	@WebMethod(operationName="GetAllDevices")
	public WsDeviceResponse getAllRegisteredDevices();
		
}
