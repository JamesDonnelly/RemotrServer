package com.matt.remotr.core.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceCoordinatorDefault;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.tcpws.TcpWsSender;
import com.matt.remotr.ws.response.WsResponse;

/**
 * The default implementation of the EventCoordinator. 
 * This is responsible for passing all incoming events on either TcpWs or SOAP Ws to the correct place
 * All devices entering this class are replaced by the instance stored in the {@link DeviceCoordinatorDefault}
 * @author mattm
 *
 */
public class EventCoordinatorDefault implements EventCoordinator {
	private Logger log;
	private TcpWsSender tcpWsSender;
	private DeviceCoordinator deviceCoordinator;
	private Map<Device, ArrayList<Event>> deviceEventMap; // Holds a map of events and what device they have originated from
	private Map<EventType, ArrayList<Device>> eventTypeDeviceMap; // Holds a list of devices that are interested in an event type
	private ArrayList<EventReceiver> eventReceiverList; // Holds a list of event receivers that have registered
	private Device systemDevice;
	
	public EventCoordinatorDefault(){
		log = Logger.getLogger(getClass());
		
		deviceEventMap = new HashMap<Device, ArrayList<Event>>();
		eventTypeDeviceMap = new HashMap<EventType, ArrayList<Device>>();
		eventReceiverList = new ArrayList<EventReceiver>();
		
	}
	
	public void setDeviceCoordinator(DeviceCoordinatorDefault deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}

	public void setTcpWsSender(TcpWsSender tcpWsSender) {
		this.tcpWsSender = tcpWsSender;
	}

	protected String getSubsystem(){
		return "EventCoordinator";
	}

	/**
	 * Allows a device to register for event notifications
	 * @param device
	 * @return
	 */
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
	
	/**
	 * Returns the cached events that have originated from a given device
	 * @param device
	 */
	@Override
	public ArrayList<Event> getEvents(Device device) {
		device = getDeviceFromCoordinator(device);
		if(device != null){
			if(deviceEventMap.containsKey(device)){
				return deviceEventMap.get(device);
			}
		}
		return null;
	}
	
	/**
	 * Can be called externally to cache an event from a device to send later. 
	 * @param event
	 * @Param device
	 * 
	 * @return int
	 */
	@Override
	public int cacheEvent(Event event, Device device){
		device = getDeviceFromCoordinator(device);
		if(device != null){
			if(deviceEventMap.containsKey(device)){
				log.debug("Device ["+device.getName()+"] is known to the EventCoordinator - Continuing to cache event");
				if(deviceEventMap.get(device).contains(event)){
					log.warn("Event ["+event.getName()+"] is already cached for Device ["+device.getName()+"]");
					return deviceEventMap.get(device).indexOf(event);
				}
			}else{
				log.debug("New device on cache event - Creating event list for device ["+device.getName()+"]");
				ArrayList<Event> eventList = new ArrayList<Event>();
				deviceEventMap.put(device, eventList);
			}
			
			return cacheEventInternal(event, device);
		}
		return -1;
	}
	
	/**
	 * Private cache function. Does no checking of device, event or the list. Must call cacheEvent
	 * @param event
	 * @param device
	 * @return int
	 */
	private int cacheEventInternal(Event event, Device device){
		log.info("Adding event ["+event.getName()+"] to cache for device ["+device.getName()+"]");
		deviceEventMap.get(device).add(event);
		return deviceEventMap.get(device).indexOf(event);		
	}
	
	/**
	 * Forwards a cached event to devices on the WsTcp Service.
	 * @param device - The device from which to take the cached event
	 * @param id - The id of the cached event
	 */
	@Override
	public void forwardEvent(Device device, int id){
		device = getDeviceFromCoordinator(device);
		if(device != null){
			Event event = deviceEventMap.get(device).get(id);
			forwardEvent(event, device);
		}
	}

	/**
	 * Forwards an event based on the @link{EventType}
	 * @param event - The event to forward
	 * @param device - The sender device
	 */
	@Override
	public void forwardEvent(Event event, Device device) {
		if(device != null){
			device = getDeviceFromCoordinator(device);
			if(event != null && !event.getName().isEmpty() && device !=null){
				log.info("Forwarding Event >> ["+event.getName()+"] via TcpWs");
				cacheEvent(event, device);
				if(event.getEventType() == EventType.BROADCAST){
					log.debug("Event ["+event.getName()+"] is a broadcast event");
					handleBroadcastEvent(event);				
				}else{
					log.debug("Event ["+event.getName()+"] is a ["+event.getEventType().toString()+"]");
					WsResponse wsResponse = new WsResponse();
					wsResponse.setResponse(event);
					wsResponse.setSubSystem("EventCoordinator");
					wsResponse.setSuccess(true);
	
					ArrayList<Device> deviceList = eventTypeDeviceMap.get(event.getEventType());
					log.debug("Found ["+deviceList.size()+"] devices registered for EventType ["+event.getEventType().toString()+"]");
					for(Device d : deviceList){
						log.debug("Forwarding to ["+d.getName()+"]");
						tcpWsSender.sendMessage(d, wsResponse);
					}
				}			
			}else{
				log.error("Error forwarding event");
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
				log.debug("Event ["+event.getName()+"] forwarded to "+eventReceiverList.size()+"] receivers");
			}
		}
	}
	
	private void handleBroadcastEvent(Event event){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setResponse(event);
		wsResponse.setSubSystem("EventCoordinator");
		wsResponse.setSuccess(true);
		tcpWsSender.sendMessage(wsResponse);
	}
	
	private Device getDeviceFromCoordinator(Device device){
		try {
			return deviceCoordinator.getDevice(device);
		} catch (DeviceException e) {
			log.error(e.getMessage());
		}
		
		return null;
	}
	
}
