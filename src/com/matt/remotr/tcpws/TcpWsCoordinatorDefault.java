package com.matt.remotr.tcpws;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceCoordinatorDefault;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.event.EventForwarder;
import com.matt.remotr.main.jaxb.JaxbFactory;
import com.matt.remotr.ws.response.domain.WsResponse;

public class TcpWsCoordinatorDefault extends Thread	implements TcpWsCoordinator {
	
	private Logger log;
    private Marshaller marshaller = null;
	private ServerSocket serverSocket;
	protected ArrayList<TcpWsServer> servers;
	protected Map<TcpWsServer, Device> serverDevice;
	protected Map<TcpWsServer, BlockingQueue<String>> serverQueue;
	
	// These can be set via spring
	private DeviceCoordinator deviceCoordinator;
	private EventForwarder eventForwarder;
	private int portNumber;
	private int maxConnections = 10;
	protected static final int SOCKET_TIMEOUT_MILIS = 60000;
	
	public TcpWsCoordinatorDefault(boolean tcpWsEnabled, int portNumber){
		log = Logger.getLogger(this.getClass());
		log.info("Initializing Tcp Ws Coordinator");
		
		this.portNumber = portNumber;
		
		if(tcpWsEnabled){
			log.info("TcpWs is enabled. Initializing...");
			serverDevice = new HashMap<TcpWsServer, Device>();
			serverQueue = new HashMap<TcpWsServer, BlockingQueue<String>>(20);
			servers = new ArrayList<TcpWsServer>();
			
			marshaller = JaxbFactory.getMarshaller();
			this.start();

		}else{
			log.info("TcpWs is set not to start");
		}
	}
	
	@Override
	public void run(){
		try{
			serverSocket = new ServerSocket(portNumber, 100);
			
			for(;;){
				// Block the thread and accept the next connection
				Socket clientSocket = serverSocket.accept();
				log.info("New TcpWs Connection");
				
				// Check if we have reached the max number of connections
				if(TcpWsServer.connectionsCount > maxConnections - 1){
					clientSocket.close();
					log.warn("Connection been closed due to maxConnections reached");
				}else{
					TcpWsServer server = new TcpWsServer(this, clientSocket, deviceCoordinator, eventForwarder);
					addServer(server);
					server.start();					
				}
			}
				
		}catch(IOException e){
			log.error("Error running server socket manager", e);
		}
	}

	private synchronized void addServer(TcpWsServer server){
		if(!servers.contains(server)){
			servers.add(server);
		}
	}

	public void setDeviceCoordinator(DeviceCoordinatorDefault deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}
	
	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	private TcpWsServer getServerForDevice(Device device){
		synchronized (serverDevice) {
			for(Entry<TcpWsServer, Device> entry : serverDevice.entrySet()){
				if(device.equals(entry.getValue())){
					return entry.getKey();
				}
			}
			
		}
		return null;
	}

	@Override
	public BlockingQueue<String> register(TcpWsServer server, Device device) {
		synchronized (serverDevice) {
			if(!serverDevice.containsKey(server)){
				serverDevice.put(server, device);
				
				synchronized (serverQueue) {
					BlockingQueue<String> queue = new LinkedBlockingQueue<String>(20);
					serverQueue.put(server, queue);
					return queue;
				}
			}else{
				return null;
			}
		}		
	}

	@SuppressWarnings("static-access")
	@Override
	public void unregister(TcpWsServer server) {
		synchronized (serverDevice) {
			if(serverDevice.containsKey(server)){
				log.debug("Unregistering server ["+server.getName()+"] from TcpWsCoordinator");
				serverDevice.remove(server);
				server.connectionsCount--;
			}
		}
		
		synchronized (serverQueue) {
			if(serverQueue.containsKey(server)){
				serverQueue.remove(server);
			}
		}
	}

	@Override
	public Boolean sendMessage(Device device, WsResponse wsResponse) {
		log.debug("Adding WsResponse from ["+wsResponse.getSubSystem()+"] to queue for device ["+device.getName()+"]");
		synchronized (serverDevice) {
			try{
				if(serverDevice.containsValue(device)){
					StringWriter sw = new StringWriter();
					marshaller.marshal(wsResponse, sw);
					TcpWsServer server = getServerForDevice(device);
					if(server != null){
						synchronized (serverQueue) {
							BlockingQueue<String> queue = serverQueue.get(server);
							queue.add(sw.toString());
							return true;
						}
					}
				}else{
					log.warn("No server found for device ["+device.getName()+"]. Message not delivered");
				}
			}catch(JAXBException je){
				log.error("JAXB Error when marshalling WsResponse", je);
			}
		}
		return false;
	}

	@Override
	public void sendMessage(WsResponse wsResponse) {
		log.debug("Adding WsResponse from ["+wsResponse.getSubSystem()+"] to queue for all devices");
		synchronized (serverQueue) {
			try{
				StringWriter sw = new StringWriter();
				marshaller.marshal(wsResponse, sw);
				for(BlockingQueue<String> bq : serverQueue.values()){
					bq.add(sw.toString());
				}
			}catch(JAXBException je){
				log.error("JAXB Error when marshalling WsResponse", je);
			}
		}
	}

	@Override
	public void sayHello(Device device) {
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(this.getClass().getName());
		wsResponse.setSuccess(true);
		
		sendMessage(device, wsResponse);
	}

	@Override
	public void sendPing(Device device) {
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem("Ping");
		wsResponse.setSuccess(true);
		
		sendMessage(device, wsResponse);		
	}
}
