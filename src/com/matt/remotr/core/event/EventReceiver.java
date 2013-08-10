package com.matt.remotr.core.event;

/**
 * Classes wishing to receive events should implement this class, then register with the event coordinator
 * @author mattm
 *
 */
public interface EventReceiver {
	
	public void onBroadcastEvent(Event event);
	
	public void onEvent(Event event);
}
