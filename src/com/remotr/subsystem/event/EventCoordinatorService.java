package com.remotr.subsystem.event;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.remotr.subsystem.device.DeviceCoordinator;
import com.remotr.subsystem.device.DeviceException;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.resource.ResourceCoordinator;
import com.remotr.subsystem.device.resource.domain.Resource;
import com.remotr.subsystem.event.types.Event;
import com.remotr.subsystem.event.types.EventType;
import com.remotr.subsystem.ws.WsBase;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsMethod;
import com.remotr.subsystem.ws.WsParam;
import com.remotr.subsystem.ws.WsRunner;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public class EventCoordinatorService extends WsBase implements WsRunner {
	
	private Logger log;	
	private EventCoordinator eventCoordinator;
	private DeviceCoordinator deviceCoordinator;
	private ResourceCoordinator resourceCoordinator;
	private WsCoordinator wsCoordinator;
	
	public EventCoordinatorService(){
		log = Logger.getLogger(this.getClass());
		subSystemName = "Event";
		log.debug("Starting new EventCoordinator WebService");
	}
	
	@PostConstruct
	public void init(){
		wsCoordinator.register(this);
	}

	@WsMethod(
			isPublic=true,
			description="Trigger an event",
			wsParams = { 
					@WsParam(name="event", type=Event.class)
			})
	public WsResponse sendEvent(Event event) {
		log.info("Incoming request to forward new event");
		WsResponse wsResponse = getWsResponseForClass();
		try{
			Resource resource = resourceCoordinator.getResource(event.getResource());
			if(resource != null){
				eventCoordinator.forwardEvent(event);
				wsResponse.setSuccess(true);
			}
		}catch(Exception e){
			log.error("Error sending message ["+event.getName()+"]", e);
			wsResponse.setErrorMessage("Error sending message ["+event.getName()+"]" + e.getMessage());
		}
		return wsResponse;
	}

	public WsResponse registerForEvents(Device device, EventType eventType) {
		log.info("Incoming request to register for events");
		WsResponse wsResponse = getWsResponseForClass();
		try{
			device = deviceCoordinator.getDevice(device);
			if(device != null){
				wsResponse.setSuccess(eventCoordinator.registerForEvents(device, eventType));
			}
		}catch(DeviceException de){
			log.error("Error when registering for event", de);
			wsResponse.setException(de);
		}
		return wsResponse;
	}

	@WsMethod(
			isPublic=true,
			description="Get events for a speciffic resource",
			wsParams = { 
					@WsParam(name="resource", type=Resource.class)
			})
	public WsResponse getEvents(Resource resource) {
		log.info("Incoming request to get events");
		WsResponse wsResponse = getWsResponseForClass();
		try{
			resource = resourceCoordinator.getResource(resource);
			if(resource != null){
				wsResponse.setListResponse(eventCoordinator.getEvents(resource));
				wsResponse.setSuccess(true);
			}
		}catch(Exception de){
			log.error("Error when getting events from resource", de);
			wsResponse.setException(de);
		}
		return wsResponse;
	}

	@Override
	public String getSubSystemName() {
		return subSystemName;
	}

	public void setEventCoordinator(EventCoordinator eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}

	public void setDeviceCoordinator(DeviceCoordinator deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}

	public void setResourceCoordinator(ResourceCoordinator resourceCoordinator) {
		this.resourceCoordinator = resourceCoordinator;
	}

	public void setWsCoordinator(WsCoordinator wsCoordinator) {
		this.wsCoordinator = wsCoordinator;
	}
	
}
