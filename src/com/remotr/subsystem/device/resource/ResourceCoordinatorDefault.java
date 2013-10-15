package com.remotr.subsystem.device.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.remotr.subsystem.device.DeviceCoordinator;
import com.remotr.subsystem.device.DeviceException;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.resource.domain.Resource;
import com.remotr.subsystem.event.EventCoordinator;
import com.remotr.subsystem.event.EventReceiver;
import com.remotr.subsystem.event.types.Event;
import com.remotr.subsystem.event.types.EventType;

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

	@Override
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
