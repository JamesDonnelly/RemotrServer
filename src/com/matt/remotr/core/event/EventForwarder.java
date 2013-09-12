package com.matt.remotr.core.event;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.domain.Resource;

/**
 * Interface for classes wishing to have the ability to forward events around the system
 * @author mattm
 *
 */
public interface EventForwarder {
	
	/**
	 * Can be called externally to cache an {@link Event} from a {@link Resource} to send later. 
	 * @param event
	 * @Param device
	 * 
	 * @return int
	 */
	public int cacheEvent(Event event);
	
	/**
	 * Forwards a cached {@link Event} to devices on the XMPP or TCPWS Service.
	 * @param id - The id of the cached {@link Event}
	 */
	public void forwardEvent(Resource resource, int id);
	
	
	/**
	 * Forwards the given event and caches it against a {@link Resource}.
	 * This method uses {@link EventType} to decide what to do with the {@link Event}
	 * @param event
	 * @param device
	 */
	public void forwardEvent(Event event);
	
	/**
	 * Forwards the given event from the {@link Resource} (and caches it) to the {@link Device}
	 * NOTE: This method does no checking of {@link EventType}
	 * @param event
	 * @param device
	 */
	public void forwardEvent(Event event, Device device);

}
