package com.remotr.subsystem.xmpp;

import java.io.StringReader;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.remotr.core.jaxb.JaxbFactory;
import com.remotr.subsystem.device.DeviceCoordinator;
import com.remotr.subsystem.device.DeviceException;
import com.remotr.subsystem.device.domain.ConnectionType;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.event.types.Event;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public class XmppMessageServer implements MessageListener, RosterListener {

	private Logger log;
	private XmppManager xmppManager;
	public static int connectionsCount = 0;
	private boolean running;
	protected Device device;
	private BlockingQueue<String> queue;
	private DeviceCoordinator deviceCoordinator;
	private Chat chat;
	
	public XmppMessageServer(XmppManager xmppCoordinator, DeviceCoordinator deviceCoordinator){
		log = Logger.getLogger(this.getClass());
		this.xmppManager = xmppCoordinator;
		this.deviceCoordinator = deviceCoordinator;
		
		log.info("Starting new XmppMessageManager");
		connectionsCount++;
		running = false;
	}
	
	@Override
	public void processMessage(Chat chat, Message message) {
		log.debug("Incoming XMPP Message from ["+message.getFrom()+"] <<<< "+message.getBody());
		this.chat = chat;
		StringReader sr = new StringReader(message.getBody());
		try {
			Unmarshaller unmarshaller = JaxbFactory.getUnmarshaller();
			Object obj = unmarshaller.unmarshal(sr);
			if(obj instanceof Device){
				device = (Device) obj;
				int atLoc = message.getFrom().indexOf("@");
				if(device.getName().equals(message.getFrom().substring(0, atLoc))){
					
					try{
						device = deviceCoordinator.getDevice(device);
						device.setConnectionType(ConnectionType.XMPP);
						try{
							deviceCoordinator.updateDevice(device);
						}catch(DeviceException de){
							log.warn("Error updating device with connection type");
						}
						log.debug("Replaced device reference with coordinator reference okay");
					}catch(DeviceException de){
						log.info("Device ["+device.getName()+"] is unknown to the Device Coordinator - Registering");
						device.setConnectionType(ConnectionType.XMPP);
						deviceCoordinator.register(device);
					}
				
					queue = xmppManager.register(this, device);
					if(queue != null){
						// Have registered okay and have been assigned a queue :)
						log.debug("Succsfully registered with manager. Starting sender service");
						running = true;
						XmppSenderService senderService = new XmppSenderService();
						senderService.start();
						xmppManager.sayHello(device);
					}
					
				}else{
					log.error("Incoming device name ["+device.getName()+"] does not match message from value ["+message.getFrom().substring(0, atLoc)+"]");
				}
			}
			
			if(obj instanceof WsRequest){
				WsRequest request = (WsRequest) obj;
				log.info("["+device.getName()+"] request to call ["+request.getMethod()+"] on ["+request.getSubSystem()+"]");
				xmppManager.handleRequest(device, request);
			}

			
			if(obj instanceof Event){
				Event event = (Event) obj;
				xmppManager.handleEvent(event);
				
				WsResponse response = new WsResponse();
				response.setReference(event.getRefference());
				response.setSubSystem("XmppCoordinator");
				response.setSuccess(true);
				xmppManager.sendResponse(device, response);
			}
			
		} catch (JAXBException e) {
			String msg = "Error unmarshalling response from ["+chat.getParticipant()+"]";
			log.error(msg);
			sendErrorResponse(msg);
		} catch (DeviceException de){
			log.error("Error contacting DeviceCoordinator");
		}
	}

	class XmppSenderService extends Thread {
		public void run(){
			this.setName("Xmpp-"+device.getName());
			try {
				while (running) {
					synchronized (queue) {
						if (!queue.isEmpty()) {
							log.debug("Taking message from xmppCallQueue");
							try {
								String message = queue.take();
	
								if (!message.equals("<E>")) {
									Message msg = new Message();
									msg.setBody(message);
									msg.setFrom("remotrServer");
									msg.setTo(device.getName());
									chat.sendMessage(msg);
									log.debug("Sent XMPP message >>>> " + message);
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
	
	@Override
	public String toString() {
		return "XmppMessageManager-"+device.getName();
	}
	
	@Override
	public void presenceChanged(Presence presence) {
		int atLoc = presence.getFrom().indexOf("@");
		String name = presence.getFrom().substring(0, atLoc);
		if(name.equals(device.getName()) && presence.getStatus() != Presence.Mode.available.toString()){
			log.info("Device ["+device.getName()+"] is no longer available on XMPP - Closing thread");
			shutdownAndCleanUp();
		}
	}
	
	private void sendErrorResponse(String msg) {
		WsResponse response = new WsResponse();
		response.setSubSystem("XmppCoordinator");
		response.setErrorMessage(msg);
		xmppManager.sendResponse(device, response);				
	}
	
	private void shutdownAndCleanUp(){
		log.info("Ending ["+chat.getParticipant()+"]");
		xmppManager.unregister(this);
		running = false;
	}
	
	// Dont really care about these...
	@Override
	public void entriesAdded(Collection<String> arg0) {}

	@Override
	public void entriesDeleted(Collection<String> arg0) {}

	@Override
	public void entriesUpdated(Collection<String> arg0) {}

}
