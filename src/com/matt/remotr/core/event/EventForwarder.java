package com.matt.remotr.core.event;

import com.matt.remotr.core.device.Device;

/**
 * Interface for classes wishing to have the ability to forward events around the system
 * @author mattm
 *
 */
public interface EventForwarder {
	
	/**
	 * Caches an event without sending it. Returns the id of the cached event for reference
	 * @param event
	 * @param device
	 * @return
	 */
	public int cacheEvent(Event event, Device device);
	
	/**
	 * Forwards the a cached event from the given device
	 * @param device
	 * @param id
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
