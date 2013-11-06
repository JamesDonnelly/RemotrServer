package com.remotr.subsystem.ws;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.remotr.subsystem.event.EventForwarder;
import com.remotr.subsystem.event.types.Event;
import com.remotr.subsystem.event.types.EventType;
import com.remotr.subsystem.ws.annotations.WsClass;
import com.remotr.subsystem.ws.annotations.WsMethod;
import com.remotr.subsystem.ws.annotations.WsParam;
import com.remotr.subsystem.ws.annotations.WsSessionKey;
import com.remotr.subsystem.ws.response.domain.WsResponse;
import com.remotr.subsystem.ws.response.domain.WsSubsystemResponse;

@WsClass(description="Handles all helper web services within Remotr")
public class WsCoordinatorService extends WsBase implements WsRunner{
	
	private WsCoordinator wsCoordinator;
	private EventForwarder eventForwarder;
	private Logger log;
	
	@WsSessionKey
	private String sessionKey;
	
	public WsCoordinatorService() {
		log = Logger.getLogger(getClass());
		subSystemName = "Service";
		log.info("Starting new Web Service WebService");
	}
	
	@PostConstruct
	public void init(){
		wsCoordinator.register(this);
	}

	@WsMethod(
			isPublic=true,
			description="Returns a list of subsystems and all methods"
			)
	public WsSubsystemResponse getSubSystems(){
		WsSubsystemResponse response = getWsSubsystemResponse();
		
		response.setSubsystemList(wsCoordinator.getSubSystemList());
		response.setSuccess(true);
		return response;
	}
	
	@WsMethod(
			isPublic=true,
			description="Echo the object back with the default fields filled",
			wsParams = { 
				@WsParam(name="object", type=Object.class)
			})
	public WsResponse echo(Object echoObject){
		WsResponse response = getWsResponse();
		response.setResponse(echoObject);
		response.setSuccess(true);
		
		return response;
	}
	
	@WsMethod(
			isPublic=true,
			description="Call to force the resource subsystem to recache it's list"
			)
	public WsSubsystemResponse forceResourceRecache(){
		Event ev = new Event();
		ev.setEventType(EventType.RESOURCE_FORCECACHE);
		ev.setName("ForceResourceRecacheEvent");
		eventForwarder.forwardEvent(ev);
		
		WsSubsystemResponse response = getWsSubsystemResponse();
		response.setSuccess(true);
		return response;
	}
	
	@WsMethod(
			isPublic=true,
			description="Call to force the device subsystem to recache it's list - This maybe unsafe"
			)
	public WsSubsystemResponse forceDeviceRecache(){
		WsSubsystemResponse response = getWsSubsystemResponse();
		response.setSuccess(true);
		return response;
	}

	@Override
	public String getSubSystemName() {
		return subSystemName;
	}

	public void setWsCoordinator(WsCoordinator wsCoordinator) {
		this.wsCoordinator = wsCoordinator;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

}
