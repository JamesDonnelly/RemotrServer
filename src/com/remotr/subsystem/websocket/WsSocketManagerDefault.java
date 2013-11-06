package com.remotr.subsystem.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.response.domain.WsResponse;

// TODO: With all coordinators really - Make manager classes, these manager classes are responsible for a number of services
//		 The manager class then talks to the coordinator. This allows splating this out over multiple servers.
//		 They will need to chatter via Event objects... (Good thing I built that bit really...)

public class WsSocketManagerDefault extends WebSocketServlet implements WsSockectManager {

	private static final long serialVersionUID = 1L;
	private Logger log;
	
	protected Map<WsSocketServer, Device> serverDevice;
	protected Map<WsSocketServer, BlockingQueue<WsResponse>> serverQueue;
	
	public WsSocketManagerDefault(){
		log = Logger.getLogger(this.getClass());
		log.info("Initializing Ws Socket Manager");
		
		serverDevice = new HashMap<WsSocketServer, Device>();
		serverQueue = new HashMap<WsSocketServer, BlockingQueue<WsResponse>>(20);
	}
	
	@Override
	public BlockingQueue<WsResponse> register(WsSocketServer wsSockectServer, Device device) {
		synchronized (serverDevice) {
			if(!serverDevice.containsKey(wsSockectServer)){
				serverDevice.put(wsSockectServer, device);
				
				synchronized (serverQueue) {
					BlockingQueue<WsResponse> queue = new LinkedBlockingQueue<WsResponse>(20);
					serverQueue.put(wsSockectServer, queue);
					return queue;
				}
			}else{
				return null;
			}
		}		
	}

	@Override
	public void unregister(WsSocketServer wsSocketServer) {
		synchronized (serverDevice) {
			if(serverDevice.containsKey(wsSocketServer)){
				log.debug("Unregistering server ["+wsSocketServer.toString()+"] from WsSocketManager");
				serverDevice.remove(wsSocketServer);
			}
		}
		
		synchronized (serverQueue) {
			if(serverQueue.containsKey(wsSocketServer)){
				serverQueue.remove(wsSocketServer);
			}
		}
	}

	@Override
	public Boolean sendResponse(Device device, WsResponse wsResponse) {
		log.debug("Adding WsResponse from ["+wsResponse.getSubSystem()+"] to queue for device ["+device.getName()+"]");
		synchronized (serverDevice) {
			try{
				if(serverDevice.containsValue(device)){
					WsSocketServer server = getServerForDevice(device);
					if(server != null){
						synchronized (serverQueue) {
							BlockingQueue<WsResponse> queue = serverQueue.get(server);
							queue.add(wsResponse);
							return true;
						}
					}
				}else{
					log.warn("No server found for device ["+device.getName()+"]. Message not delivered");
				}
			}catch(Exception e){
				log.error("Error while adding WsResponse to queue", e);
			}
		}
		return false;
	}

	@Override
	public void sendResponse(WsResponse wsResponse) {
		log.debug("Adding WsResponse from ["+wsResponse.getSubSystem()+"] to queue for all devices");
		synchronized (serverQueue) {
			try{
				for(BlockingQueue<WsResponse> bq : serverQueue.values()){
					bq.add(wsResponse);
				}
			}catch(Exception e){
				log.error("Error while adding WsResponse to queue", e);
			}
		}
	}
	
	private WsSocketServer getServerForDevice(Device device){
		synchronized (serverDevice) {
			for(Entry<WsSocketServer, Device> entry : serverDevice.entrySet()){
				if(device.equals(entry.getValue())){
					return entry.getKey();
				}
			}
			
		}
		return null;
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		 factory.getPolicy().setIdleTimeout(60000);
	     factory.register(WsSocketServer.class);
	}

}
