package com.matt.remotr.tcpws;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.event.Event;
import com.matt.remotr.core.event.EventForwarder;
import com.matt.remotr.main.jaxb.JaxbFactory;
import com.matt.remotr.ws.response.WsResponse;

public class TcpWsServer extends Thread {
	public static int connectionsCount = 0;
	public static int HEARTBEAT_WAIT = 60000;
	private static int TIMEOUT_MILIS = 120000;
	
	private Logger log;
	private Boolean running;
	private TcpWsCoordinator tcpWsCoordinator;
	private DeviceCoordinator deviceCoordinator;
	private EventForwarder eventForwarder;
	private Socket clientSocket;
	private BufferedWriter dataOutput;
	private InputStreamReader dataInput;
	private BlockingQueue<String> queue;
	private Device device;
	
	public TcpWsServer(TcpWsCoordinator tcpWsCoordinator, Socket clientSocket, DeviceCoordinator deviceCoordinator, EventForwarder eventForwarder){
		log = Logger.getLogger(getClass());
		this.tcpWsCoordinator = tcpWsCoordinator;
		this.clientSocket = clientSocket;
		this.deviceCoordinator = deviceCoordinator;
		this.eventForwarder = eventForwarder;
		
		// Set a temp name until we know what device we are dealing with
		setName("TcpWsThread-" +this.clientSocket.getRemoteSocketAddress());
		log.info("Starting TcpWsServer and services for ["+this.clientSocket.getRemoteSocketAddress()+"]");
		
		try{
			this.clientSocket.setSoTimeout(TIMEOUT_MILIS);
			connectionsCount++;
			
			log.info("Number of TcpWs Connections ["+connectionsCount+"]");
		}catch(SocketException se){
			log.error("Unable to set socket timeout");
		}
	}
	
	protected void shutdownAndCleanUp(){
		try{
			log.info("Ending [" + this.getName() + "]");
			unregisterFromCoordinator();
			dataOutput.close();
			dataInput.close();
			clientSocket.close();
			running = false;
		}catch(IOException ioe){
			log.warn("There was an error shutting down the TcpWsSenderService for ["+device.getName()+"]");
		}
	}
	
	private void startTcpWsSenderService(){
		TcpWsSenderService senderService =  new TcpWsSenderService();
		senderService.start();
		senderService.setName("TcpWsSenderService-"+device.getName());
	}
	
	private void startTcpWsHeartbeatService(){
		TcpWsHeartbeatService heartbeatService = new TcpWsHeartbeatService();
		heartbeatService.start();
		heartbeatService.setName("TcpWsHeartbeatService-"+device.getName());
	}
	
	private void unregisterFromCoordinator(){
		tcpWsCoordinator.unregister(this);
	}
	
	public void run(){
		running = true;
		try{
			dataOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
			dataInput = new InputStreamReader(clientSocket.getInputStream(), "UTF-8");
		}catch(Exception e){
			log.warn("General Error ["+e.getMessage()+"]");
		}
		
		StringBuilder incomingChars = null;
		while(running){
			try{
				int c;
				incomingChars = new StringBuilder();
				while((c = dataInput.read()) > 0){
					incomingChars.append((char)c);
				}
			
				if(incomingChars != null){
					log.debug("Incoming TCP Message <<<< "+incomingChars.toString());
					StringReader sr = new StringReader(incomingChars.toString());
					
					try{
						Unmarshaller unmarshaller = JaxbFactory.getUnmarshaller();
						Object obj = unmarshaller.unmarshal(sr);
						
						if(obj instanceof Device){
							try{
								device = (Device) obj;
								device = deviceCoordinator.getDevice(device);
								log.debug("Replaced device reference with coordinator reference okay");
							}catch(DeviceException de){
								log.info("Device ["+device.getName()+"] is unknown to the Device Coordinator - Registering");
								deviceCoordinator.register(device);
							}
							
							queue = tcpWsCoordinator.register(this, device);
							if(queue != null){
								// Have registered okay and have been assigned a queue :)
								log.debug("Succsfully registered with coordinator. Starting sender service");
								this.setName("TcpWs-"+device.getName());
								startTcpWsSenderService();
								startTcpWsHeartbeatService();
							}
						}
						
						if(obj instanceof WsResponse){
							WsResponse wsResponse = (WsResponse) obj;
							if(wsResponse.getSubSystem().equalsIgnoreCase("pong")){
								if(wsResponse.getResponse() != null){
									log.warn("Discarding wrapped response object");
								}
							deviceCoordinator.setHeartbeatTime(device);
							}
						}
						
						if(obj instanceof Event){
							Event event = (Event) obj;
							eventForwarder.forwardEvent(event, device);
						}
						
					}catch(JAXBException je){
						log.error("Error unmarshalling incoming object", je);
						log.info("Closing connection");
						shutdownAndCleanUp();
					}
					
					if(incomingChars.toString().equals("")){
						shutdownAndCleanUp();
					}
				}  
			}catch(Exception e){
				log.warn("Error ["+e.getMessage()+"]");
				shutdownAndCleanUp();
		    }
		}
	}
	
	class TcpWsSenderService extends Thread {
		public void run(){
			try {
				log.info("Initializing [" + this.getName() + "]");
				log.debug("Saying hello to client");
				
				// Say hello :)
				tcpWsCoordinator.sayHello(device);
				
				while (running) {
					synchronized (queue) {
						if (!queue.isEmpty()) {
							log.debug("Taking message from wsCallQueue");
							try {
								String message = queue.take();

								if (!message.equals("<E>")) {

									dataOutput.write(message);
									dataOutput.write(0);
									dataOutput.flush();

									log.debug("Sent TCP message >>>> " + message);
								} else {
									shutdownAndCleanUp();
								}
							} catch (Exception e) {
								log.error("Error taking message from queue", e);
							}
						}
						try {
							synchronized (this) {
								wait(1000);
							}
						} catch (InterruptedException e) {
							log.error("Sleep interrupted", e);
						}
					}
				}
			} catch (Exception e) {
				log.error("An error happened...", e);
				shutdownAndCleanUp();
			}
		}
	}
	
	class TcpWsHeartbeatService extends Thread {
		public void run(){
			try {
				log.info("Initializing [" + this.getName() + "]");
				
				while(running){
					// Send them every few mins
					synchronized (this) {
						sleep(HEARTBEAT_WAIT);
					}
					
					tcpWsCoordinator.sendPing(device);
					
					// Give the device some time to respond
					synchronized (this) {
						sleep(30000);
					}
					
					Device d = deviceCoordinator.getDevice(device);
					if(d.getLastHeartbeatTime() < (System.currentTimeMillis() - 40000)){
						log.warn("Device ["+d.getName()+"] has not responded to heartbeats in a timely way");
						shutdownAndCleanUp();
					}else{
						log.debug("Heartbeat okay from ["+d.getName()+"]");
						device = d; // refresh our local reference
					}
				}
			}catch(Exception e ){
				shutdownAndCleanUp();
			}
		}
	}
	
	
}

