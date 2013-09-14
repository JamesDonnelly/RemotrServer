package com.matt.remotr.core.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.EventCoordinator;
import com.matt.remotr.core.event.EventReceiver;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.domain.Resource;

/**
 * The {@link ResourceCoordinatorDefault} is responsible for managing the resources associated with each {@link Device} on the Server.
 * It holds a cached list of all the {@link Resource} objects. This list is refreshed when the coordinator is notified on a {@link Device}
 * update {@link Event}
 * @author matt
 *
 */
public class ResourceCoordinatorDefault implements ResourceCoordinator, EventReceiver {

	private Logger log;
	private EventCoordinator eventCoordinator;
	private DeviceCoordinator deviceCoordinator;
	
	private Map<Long, Resource> resourceMap;
	
	public ResourceCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		resourceMap = new HashMap<Long, Resource>();
	}
	
	@PostConstruct
	public void init(){
		eventCoordinator.registerForEvents(this);
		triggerRecache();
	}
	
	public void setEventCoordinator(EventCoordinator eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}

	public void setDeviceCoordinator(DeviceCoordinator deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}

	@Override
	public Resource getResource(Long id) {
		log.debug("Incoming request to get cached resource with Id ["+id+"]");
		if(resourceMap.containsKey(id)){
			log.debug("Found resource - returning now");
			return resourceMap.get(id);
		}
		return null;
	}
	
	@Override
	public Resource getResource(Resource resource) {
		log.debug("Incoming request to get cached resource with Id ["+resource.getId()+"]");
		if(resourceMap.containsValue(resource)){
			log.debug("Found resource - returning now");
			return resourceMap.get(resource.getId());
		}
		return null;
	}

	@Override
	public ArrayList<Resource> getResource(Device device) {
		log.debug("Incoming request to get cached resources for device ["+device.getName()+"]");
		try {
			device = deviceCoordinator.getDevice(device);
			if(device != null){
				return device.getResources();
			}
		} catch (DeviceException e) {
			log.error("Error getting device from coordinator", e);
		}
		return null;
	}

	@Override
	public void addResource(Resource resource, Device device) throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeResource(Long id) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onBroadcastEvent(Event event) {

	}

	@Override // TODO: React to device deregister
	public void onEvent(Event event) {
		if(event.getEventType().equals(EventType.DEVICE_REGISTER) || 
				event.getEventType().equals(EventType.DEVICE_UPDATE) || 
				event.getEventType().equals(EventType.DEVICE_UNREGISTER)){
			triggerRecache();
		}
	}
	
	private void triggerRecache(){
		if(resourceMap != null && deviceCoordinator != null){
			log.info("Recache of resources triggered");
			resourceMap.clear();
			ArrayList<Device> deviceList = deviceCoordinator.getAllRegisteredDevices();
			if(deviceList != null){
				for(Device d : deviceList){
					ArrayList<Resource> resourceList = d.getResources();
					if(resourceList != null){
						for(Resource r : resourceList){
							resourceMap.put(r.getId(), r);
						}
					}
				}
			}
		}
	}

}
