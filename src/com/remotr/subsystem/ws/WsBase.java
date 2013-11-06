package com.remotr.subsystem.ws;

import org.apache.log4j.Logger;

import com.remotr.core.Main;
import com.remotr.subsystem.ws.annotations.WsMethod;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsResponse;
import com.remotr.subsystem.ws.response.domain.WsSubsystemResponse;

/**
 * Base abstract class for all classes exposing {@link WsMethod}s to extend. 
 * This class should be extended by the service classes, these service classes should then be responsible for sending the correct data
 * to the class that the service class is helping, and then wrapping the response in a {@link WsResponse} object, before passing this
 * back to the {@link WsCoordinator} (via a return). 
 * @author matt
 *
 */
public abstract class WsBase {
	protected Logger log;
	protected String subSystemName;
	
	public WsBase(){
		log = Logger.getLogger(this.getClass());
	}
	
	protected WsResponse getWsResponse(){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(subSystemName);
		
		wsResponse.setVersionName(Main.getVersionName());
		wsResponse.setVersionNum(Main.getVersionNumber());
		
		return wsResponse;
	}
	
	protected WsResponse getWsResponse(String calledMethodName){
		WsResponse r = getWsResponse();
		r.createSubsystemHolder(subSystemName, calledMethodName);
		
		return r;
	}
	
	protected WsResponse getWsResponse(WsRequest request){
		WsResponse r = getWsResponse();
		r.createSubsystemHolder(request);
		
		return r;
	}
	
	protected WsSubsystemResponse getWsSubsystemResponse() {
		WsSubsystemResponse response = new WsSubsystemResponse();
		response.setSubSystem(subSystemName);
		
		response.setVersionName(Main.getVersionName());
		response.setVersionNum(Main.getVersionNumber());
		
		return response;
	}
	
}
