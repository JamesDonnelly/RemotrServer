package com.matt.remotr.xmpp;

import java.util.concurrent.BlockingQueue;

import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.tcpws.WsSender;

/**
 * Handles access to the system via XMPP Messages. 
 * This is used for devices that want to interact with the service, but not via Tcp and reading the WSDL (Lazy bastards).
 * @author mattm
 *
 */
public interface XmppCoordinator extends WsSender {
	
	/**
	 * Register a {@link XmppMessageManager} with the {@link XmppCoordinator} for a specific device
	 * If the registration is successful, then a queue is returned for that server to use.
	 * @param server
	 * @param device
	 * @return BlockingQueue
	 */
	public BlockingQueue<String> register(XmppMessageManager messageManager, Device device);
	
	/**
	 * Unregister a {@link XmppMessageManager} from the coordinator
	 * @param server
	 */
	public void unregister(XmppMessageManager messageManager);
		
	/**
	 * Send a ping message to a device. It is up to the calling class to listen to the response
	 * @param device
	 */
	public void sendPing(Device device);
	
	/**
	 * Can be called by the server once registration is done to say hello to the remote client. 
	 * Lets them know that all went well
	 */
	public void sayHello(Device device);
	
	/**
	 * Handles an incoming event received from the {@link XmppMessageManager}
	 * @param device
	 * @param event
	 */
	public void handleEvent(Device device, Event event);

}
