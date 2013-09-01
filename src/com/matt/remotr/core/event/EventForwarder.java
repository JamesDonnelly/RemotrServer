package com.matt.remotr.core.event;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.core.event.types.EventType;

/**
 * Interface for classes wishing to have the ability to forward events around the system
 * @author mattm
 *
 */
public interface EventForwarder {
	
	/**
	 * Can be called externally to cache an event from a device to send later. 
	 * @param event
	 * @Param device
	 * 
	 * @return int
	 */
	public int cacheEvent(Event event, Device device);
	
	/**
	 * Forwards a cached event to devices on the XMPP or TCPWS Service.
	 * @param device - The device from which to take the cached event
	 * @param id - The id of the cached event
	 */
	public void forwardEvent(Device device, int id);
	
	
	/**
	 * Forwards the given event and caches it against the given device.
	 * This method uses {@link EventType} to decide what to do with the event
	 * @param event
	 * @param device
	 */
	public void forwardEvent(Event event, Device device);
	
	/**
	 * Forwards the given event from the device (and caches it) to the second device
	 * NOTE: This method does no checking of {@link EventType}
	 * @param event
	 * @param fromDevice
	 * @param toDevice
	 */
	public void forwardEvent(Event event, Device fromDevice, Device toDevice);

}
