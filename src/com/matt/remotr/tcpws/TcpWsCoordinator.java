package com.matt.remotr.tcpws;

import java.util.concurrent.BlockingQueue;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.ws.WsSender;

/**
 * Interface for classes implementing TCP Ws coordination. If the implementing class only wants to implement sending, then implement {@link WsSender}
 * @author mattm
 *
 */
public interface TcpWsCoordinator extends WsSender{
	
	/**
	 * Register a server with the {@link TcpWsCoordinator} for a specific device
	 * If the registration is successful, then a queue is returned for that server to use.
	 * @param server
	 * @param device
	 * @return BlockingQueue
	 */
	public BlockingQueue<String> register(TcpWsServer server, Device device);
	
	/**
	 * Unregister a server from the coordinator
	 * @param server
	 */
	public void unregister(TcpWsServer server);
	
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

}
