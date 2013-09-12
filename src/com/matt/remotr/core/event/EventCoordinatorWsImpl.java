package com.matt.remotr.core.event;

import javax.annotation.PostConstruct;
import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.ResourceCoordinator;
import com.matt.remotr.core.resource.domain.Resource;
import com.matt.remotr.main.Main;
import com.matt.remotr.ws.EventCoordinatorWs;
import com.matt.remotr.ws.request.WsRequestManager;
import com.matt.remotr.ws.response.domain.WsResponse;

@WebService(targetNamespace="http://remotr.org/wsdl", endpointInterface="com.matt.remotr.ws.EventCoordinatorWs", serviceName="event")
public class EventCoordinatorWsImpl extends SpringBeanAutowiringSupport implements EventCoordinatorWs {
	
	private Logger log;	
	@Autowired
	private EventCoordinator eventCoordinator;
	@Autowired
	private DeviceCoordinator deviceCoordinator;
	@Autowired
	private ResourceCoordinator resourceCoordinator;
	@Autowired
	private WsRequestManager requestManager;
	
	public EventCoordinatorWsImpl(){
		log = Logger.getLogger(this.getClass());
		log.debug("Starting new EventCoordinator WebService");
	}
	
	@PostConstruct
	public void init(){
		requestManager.register(this);
	}

	@Override
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

	@Override
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

	@Override
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
	
	private WsResponse getWsResponseForClass(){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(getSubSystemName());
		wsResponse.setVersionName(Main.getVersionName());
		wsResponse.setVersionNum(Main.getVersionNumber());
		return wsResponse;
	}

	@Override
	public String getSubSystemName() {
		return "EventCoordinator";
	}
	
}
