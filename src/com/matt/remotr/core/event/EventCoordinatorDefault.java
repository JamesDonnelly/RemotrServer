package com.matt.remotr.core.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceCoordinatorDefault;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.device.domain.ConnectionType;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.job.JobForwarder;
import com.matt.remotr.core.job.RemotrJob;
import com.matt.remotr.core.resource.ResourceCoordinator;
import com.matt.remotr.core.resource.domain.Resource;
import com.matt.remotr.main.Main;
import com.matt.remotr.ws.WsSender;
import com.matt.remotr.ws.response.WsJobResponse;
import com.matt.remotr.ws.response.WsResponseForwarder;
import com.matt.remotr.ws.response.domain.WsResponse;

/**
 * The default implementation of the EventCoordinator. 
 * This is responsible for passing all incoming events and commands (running in jobs) on either TcpWs, XMPP or SOAP Ws 
 * to the correct place. Events are cached locally against the resource they originated from.
 * All devices entering this class are replaced by the instance stored in the {@link DeviceCoordinatorDefault}
 * @author mattm
 *
 */
public class EventCoordinatorDefault implements EventCoordinator, JobForwarder, WsResponseForwarder {
	private Logger log;
	private WsSender tcpWsSender;
	private WsSender xmppWsSender;
	private DeviceCoordinator deviceCoordinator;
	private ResourceCoordinator resourceCoordinator;
	
	// TODO: This should be Resource aware and not cached against devices
	private Map<Resource, ArrayList<Event>> resourceEventMap; // Holds a map of events and what resource they have originated from
	private Map<EventType, ArrayList<Device>> eventTypeDeviceMap; // Holds a list of devices that are interested in an event type
	private ArrayList<EventReceiver> eventReceiverList; // Holds a list of event receivers that have registered
	
	public EventCoordinatorDefault(){
		log = Logger.getLogger(getClass());
		
		resourceEventMap = new HashMap<Resource, ArrayList<Event>>();
		eventTypeDeviceMap = new HashMap<EventType, ArrayList<Device>>();
		eventReceiverList = new ArrayList<EventReceiver>();
	}
	
	public void setDeviceCoordinator(DeviceCoordinator deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}
	
	public void setResourceCoordinator(ResourceCoordinator resourceCoordinator){
		this.resourceCoordinator = resourceCoordinator;
	}

	public void setTcpWsSender(WsSender tcpWsSender) {
		this.tcpWsSender = tcpWsSender;
	}

	public void setXmppWsSender(WsSender xmppWsSender) {
		this.xmppWsSender = xmppWsSender;
	}

	protected String getSubsystem(){
		return "EventCoordinator";
	}

	@Override
	public boolean registerForEvents(Device device, EventType eventType){
		device = getDeviceFromCoordinator(device);
		if(device != null){
			if(eventTypeDeviceMap.containsKey(eventType)){
				log.debug("EventType ["+eventType.toString()+"] is known to the Event Coordinator - Checking device");
				if(eventTypeDeviceMap.get(eventType).contains(device)){
					log.warn("EventType ["+eventType.toString()+"] is already assoicated with Device ["+device.getName()+"]");
					return false;
				}
			}else{
				log.debug("New EventType - Creating device list for EventType ["+eventType.toString()+"]");
				ArrayList<Device> eventList = new ArrayList<Device>();
				eventTypeDeviceMap.put(eventType, eventList);
			}
			
			return registerForEventsInternal(device, eventType);
		}
		return false;
	}

	@Override
	public void registerForEvents(EventReceiver eventReceiver) {
		if(!eventReceiverList.contains(eventReceiver)){
			log.debug("New EventReceiver registered");
			eventReceiverList.add(eventReceiver);
		}
	}
	
	private boolean registerForEventsInternal(Device device, EventType eventType){
		log.info("Registered device ["+device.getName()+"] for eventType ["+eventType.toString()+"]");
		return eventTypeDeviceMap.get(eventType).add(device);		
	}
	
	@Override
	public ArrayList<Event> getEvents(Resource resource) {
		resource = getResourceFromCoordinator(resource);
		if(resource != null && resourceEventMap.containsKey(resource)){
			return resourceEventMap.get(resource);
		}
		return null;
	}

	@Override
	public int cacheEvent(Event event){
		Resource resource = getResourceFromCoordinator(event.getResource());
		if(resource != null){
			if(resourceEventMap.containsKey(resource)){
				log.debug("Resource ["+resource.getResourceName()+"] is known to the EventCoordinator - Continuing to cache event");
				if(resourceEventMap.get(resource).contains(event)){
					log.warn("Event ["+event.getName()+"] is already cached for resource ["+resource.getResourceName()+"]");
					return resourceEventMap.get(resource).indexOf(event);
				}
			}else{
				log.debug("New resource on cache event - Creating event list for resource ["+resource.getResourceName()+"]");
				ArrayList<Event> eventList = new ArrayList<Event>();
				resourceEventMap.put(resource, eventList);
			}
			
			return cacheEventInternal(event, resource);
		}
		return -1;
	}
	
	private int cacheEventInternal(Event event, Resource resource){
		log.info("Adding event ["+event.getName()+"] to cache for resource ["+resource.getResourceName()+"]");
		resourceEventMap.get(resource).add(event);
		return resourceEventMap.get(resource).indexOf(event);		
	}
	
	@Override
	public void forwardEvent(Resource resource, int id){
		resource = getResourceFromCoordinator(resource);
		if(resource != null){
			Event event = resourceEventMap.get(resource).get(id);
			forwardEvent(event);
		}
	}

	@Override
	public void forwardEvent(Event event) {
		if(event.getResource() != null){
			Resource resource = getResourceFromCoordinator(event.getResource());
			if(event != null && !event.getName().isEmpty() && resource != null){
				log.info("Forwarding Event >> ["+event.getName()+"]");
				cacheEvent(event);
				if(event.getEventType() == EventType.BROADCAST){
					log.debug("Event ["+event.getName()+"] is a broadcast event");
					handleBroadcastEvent(event);
				}else if(event.getEventType() == EventType.JOB){
					log.debug("Event ["+event.getName()+"] is a job (probably a response) event");
					handleJobEvent(event);
				}else{
					WsResponse wsResponse = wrapEvent(event);
	
					ArrayList<Device> deviceList = eventTypeDeviceMap.get(event.getEventType());
					if(deviceList != null){
						log.debug("Found ["+deviceList.size()+"] devices registered for EventType ["+event.getEventType().toString()+"]");
						for(Device d : deviceList){
							log.debug("Forwarding to ["+d.getName()+"]");
							sendMessage(d, wsResponse);
						}
					}
				}			
			}else{
				log.error("No resource has been defined for event ["+event.getName()+"]");
			}
		}else{ // device is null, so this is internal
			if(event != null && !event.getName().isEmpty()){
				log.info("Forwarding Event >> ["+event.getName()+"] internally");
				for(EventReceiver er : eventReceiverList){
					if(event.getEventType() == EventType.BROADCAST){
						er.onBroadcastEvent(event);
					}else{
						er.onEvent(event);
					}
				}
				log.debug("Event ["+event.getName()+"] forwarded to ["+eventReceiverList.size()+"] receivers");
			}
		}
	}
	
	@Override
	public void forwardEvent(Event event, Device device) {
		Resource resource = getResourceFromCoordinator(event.getResource());
		if(device != null && resource != null && event != null){
			device = getDeviceFromCoordinator(device);
			resource = getResourceFromCoordinator(resource);
			log.info("Forwarding Event >> ["+event.getName()+"] to ["+device.getName()+"]");
			cacheEvent(event);
			
			WsResponse wsResponse = wrapEvent(event);
			sendMessage(device, wsResponse);	
		}
	}

	@Override
	public void forwardJob(Device device, RemotrJob job) {
		if(device !=null){
			device = getDeviceFromCoordinator(device);
			if(device != null || job != null){
				log.info("Forwarding RemotrJob >> ["+job.getJobName()+"]");
				// Jobs are not cached
				WsJobResponse jobResponse = new WsJobResponse();
				jobResponse.setCommand(job.getCommand());
				jobResponse.setCronExpression(job.getCronExpression());
				jobResponse.setJobName(job.getJobName());
				jobResponse.setJobStatus(job.getJobStatus());
				
				jobResponse.setSubSystem("JobCoordinator");
				jobResponse.setSuccess(true);
				
				log.debug("Forwarding to ["+device.getName()+"]");
				sendMessage(device, jobResponse);				
			}
		}else{
			log.error("Error forwarding job");
		}
	}
	
	@Override
	public void forwardWsResponse(Device device, WsResponse wsResponse) {
		device = getDeviceFromCoordinator(device);
		if(device != null){
			log.info("Forwarding WsResponse >> ["+wsResponse.getReference()+"]");
			// Override the version info on the response...
			wsResponse.setVersionName(Main.getVersionName());
			wsResponse.setVersionNum(Main.getVersionNumber());
			sendMessage(device, wsResponse);
		}
	}
	
	private WsResponse wrapEvent(Event event){
		log.debug("Event ["+event.getName()+"] is a ["+event.getEventType().toString()+"]");
		WsResponse wsResponse = new WsResponse();
		wsResponse.setResponse(event);
		wsResponse.setSubSystem("EventCoordinator");
		wsResponse.setSuccess(true);

		return wsResponse;
	}
	
	private void handleBroadcastEvent(Event event){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setResponse(event);
		wsResponse.setSubSystem("EventCoordinator");
		wsResponse.setSuccess(true);
		sendMessage(wsResponse);
	}
	
	private void handleJobEvent(Event event) {
		forwardEvent(event, null);		
	}
	
	private Device getDeviceFromCoordinator(Device device) {
		try {
			return deviceCoordinator.getDevice(device);
		} catch (DeviceException e) {
			log.error(e.getMessage());
		}
		
		return null;
	}
	
	private Resource getResourceFromCoordinator(Resource resource){
		try {
			if(resource != null){
				return resourceCoordinator.getResource(resource);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return null;
	}
	
	private void sendMessage(Device device, WsResponse wsResponse){
		if(device.getConnectionType() == ConnectionType.TCPWS){
			tcpWsSender.sendMessage(device, wsResponse);
		}else{
			xmppWsSender.sendMessage(device, wsResponse);
		}
	}
	
	private void sendMessage(WsResponse wsResponse){
		tcpWsSender.sendMessage(wsResponse);
		xmppWsSender.sendMessage(wsResponse);
	}
	
}
