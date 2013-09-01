package com.matt.remotr.core.event;

import java.util.ArrayList;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;

/**
 * All the EventCoordinator methods. Classes only interested in providing event forwarding should implement {@link EventForwarder}
 * @author mattm
 *
 */
public interface EventCoordinator extends EventForwarder {
	
	public boolean registerForEvents(Device device, EventType eventType);
	
	public void registerForEvents(EventReceiver eventReceiver);
	
	public ArrayList<Event> getEvents(Device device);

}
