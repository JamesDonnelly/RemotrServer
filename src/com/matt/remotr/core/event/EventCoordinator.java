package com.matt.remotr.core.event;

import java.util.ArrayList;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.domain.Resource;

/**
 * All the EventCoordinator methods. Classes only interested in providing event forwarding should implement {@link EventForwarder}
 * @author mattm
 *
 */
public interface EventCoordinator extends EventForwarder {
	
	/**
	 * Registers a {@link Device} for a given {@link EventType}
	 * @param device
	 * @param eventType
	 * @return
	 */
	public boolean registerForEvents(Device device, EventType eventType);
	
	/**
	 * Registers a class that implements the {@link EventReceiver} to receive {@link Event}
	 * @param eventReceiver
	 */
	public void registerForEvents(EventReceiver eventReceiver);
	
	/**
	 * Gets all {@link Event}s for the passed {@link Resource}
	 * @param resource
	 * @return
	 */
	public ArrayList<Event> getEvents(Resource resource);

}
