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
import com.remotr.subsystem.ws.WsRunner;
import com.remotr.subsystem.ws.annotations.WsClass;
import com.remotr.subsystem.ws.annotations.WsMethod;
import com.remotr.subsystem.ws.annotations.WsParam;
import com.remotr.subsystem.ws.annotations.WsSessionKey;
import com.remotr.subsystem.ws.response.domain.WsResponse;

@WsClass(description="Handles all external events and registrations")
public class EventCoordinatorService extends WsBase implements WsRunner {
	
	private Logger log;	
	private EventCoordinator eventCoordinator;
	private DeviceCoordinator deviceCoordinator;
	private ResourceCoordinator resourceCoordinator;
	private WsCoordinator wsCoordinator;
	
	@WsSessionKey
	private String sessionKey;
	
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
			isPublic=false,
			description="Trigger an event",
			wsParams = { 
					@WsParam(name="event", type=Event.class)
			})
	public WsResponse sendEvent(Event event) {
		log.info("Incoming request to forward new event");
		WsResponse wsResponse = getWsResponse();
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

	@WsMethod(
			isPublic=false,
			isAsync=true,
			description="Register a device for a certian event type",
			wsParams = { 
					@WsParam(name="event", type=Event.class)
			})
	public WsResponse registerForEvents(Event event) {
		log.info("Incoming request to register for events");
		WsResponse wsResponse = getWsResponse();
		try{
			Device device = deviceCoordinator.getDeviceBySessionKey(sessionKey);
			device = deviceCoordinator.getDevice(device);
			if(device != null){
				EventType eventType = event.getEventType();
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
		WsResponse wsResponse = getWsResponse();
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
	
	@WsMethod(
			isPublic=true,
			description="Send a ping to all devices registered for broadcast events"
			)
	public WsResponse sendPingEvent(){
		log.info("Incoming request to send ping event");
		WsResponse response = getWsResponse();
		
		Event ev = new Event();
		ev.setEventType(EventType.BROADCAST);
		ev.setName("Ping");
		
		try {
			Device sysDevice = deviceCoordinator.getSystemDevice();
			ev.setResource(sysDevice.getResources().get(0));
			
			eventCoordinator.forwardEvent(ev);
			
			response.setSuccess(true);
		} catch (DeviceException e) {
			log.error("Error getting system device from coordinator");
		}

		return response;
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
