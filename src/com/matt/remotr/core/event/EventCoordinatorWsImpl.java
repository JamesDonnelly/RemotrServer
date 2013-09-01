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
	public WsResponse sendEvent(Event event, Device device) {
		log.info("Incoming request to forward new event");
		WsResponse wsResponse = getWsResponseForClass();
		// Fetch the full device object from the coordinator
		try{
			device = deviceCoordinator.getDevice(device);
			if(device != null){
				eventCoordinator.forwardEvent(event, device);
				wsResponse.setSuccess(true);
			}
		}catch(Exception e){
			log.error("Error sending message ["+event.getName()+"] from device ["+device.getName()+"]", e);
			wsResponse.setErrorMessage("Error sending message ["+event.getName()+"] to device ["+device.getName()+"]" + e.getMessage());
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
	public WsResponse getEvents(Device device) {
		log.info("Incoming request to get events");
		WsResponse wsResponse = getWsResponseForClass();
		try{
			device = deviceCoordinator.getDevice(device);
			if(device != null){
				wsResponse.setListResponse(eventCoordinator.getEvents(device));
				wsResponse.setSuccess(true);
			}
		}catch(DeviceException de){
			log.error("Error when getting events from device", de);
			wsResponse.setException(de);
		}
		return wsResponse;
	}
	
	private WsResponse getWsResponseForClass(){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(getSubSystemName());
		return wsResponse;
	}

	@Override
	public String getSubSystemName() {
		return "EventCoordinator";
	}
	
}
