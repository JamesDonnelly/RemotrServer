package com.remotr.subsystem.tcpws;

import java.util.concurrent.BlockingQueue;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.WsEndpointProvider;
import com.remotr.subsystem.ws.WsSender;

/**
 * Interface for classes implementing TCP Ws coordination. 
 * If the implementing class only wants to implement sending, then implement {@link WsSender}
 * @author mattm
 *
 */
public interface TcpWsManager extends WsEndpointProvider{
	
	/**
	 * Register a server with the {@link TcpWsManager} for a specific device
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
