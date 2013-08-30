package com.matt.remotr.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.event.EventType;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.ws.response.WsResponse;

@WebService(targetNamespace="http://remotr.org/wsdl")
public interface EventCoordinatorWs {
	
	@WebMethod(operationName="RegisterForEvents")
	public WsResponse registerForEvents(@WebParam(name="Device") Device device, @WebParam(name="EventType") EventType eventType);
	
	@WebMethod(operationName="GetEvents")
	public WsResponse getEvents(@WebParam(name="Device") Device device);
	
	@WebMethod(operationName="SendEvent")
	public WsResponse sendEvent(@WebParam(name="Event") Event event, @WebParam(name="Device") Device device);

}
