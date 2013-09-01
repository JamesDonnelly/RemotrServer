package com.matt.remotr.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.ws.response.domain.WsResponse;

@WebService(targetNamespace="http://remotr.org/wsdl")
public interface CommandManagerWs {
	
	@WebMethod(operationName="TriggerCommand")
	public WsResponse triggerCommand(@WebParam(name="Device") Device device);

}
