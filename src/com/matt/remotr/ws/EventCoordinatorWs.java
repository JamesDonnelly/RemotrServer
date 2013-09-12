package com.matt.remotr.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.domain.Resource;
import com.matt.remotr.ws.request.WsRequestRunner;
import com.matt.remotr.ws.response.domain.WsResponse;

@WebService(targetNamespace="http://remotr.org/wsdl")
public interface EventCoordinatorWs extends WsRequestRunner {
	
	@WebMethod(operationName="RegisterForEvents")
	public WsResponse registerForEvents(@WebParam(name="Device") Device device, @WebParam(name="EventType") EventType eventType);
	
	@WebMethod(operationName="GetEvents")
	public WsResponse getEvents(@WebParam(name="Resource") Resource resource);
	
	@WebMethod(operationName="SendEvent")
	public WsResponse sendEvent(@WebParam(name="Event") Event event);

}
