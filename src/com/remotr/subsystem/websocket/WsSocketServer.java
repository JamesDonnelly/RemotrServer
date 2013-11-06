package com.remotr.subsystem.websocket;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.remotr.core.jaxb.JaxbFactory;
import com.remotr.subsystem.device.DeviceCoordinator;
import com.remotr.subsystem.device.DeviceException;
import com.remotr.subsystem.device.domain.ConnectionType;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.session.SessionCoordinator;
import com.remotr.subsystem.session.domain.DeviceSession;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsException;
import com.remotr.subsystem.ws.WsResponseReceiver;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsResponse;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WsSocketServer extends SpringBeanAutowiringSupport implements WsResponseReceiver {
	
	@Autowired
	private WsCoordinator wsCoordinator;
	
	@Autowired
	private SessionCoordinator sessionCoordinator;
	
	@Autowired
	private DeviceCoordinator deviceCoordinator;
	
	@Autowired
	private WsSockectCoordinator socketCoordinator;
	
	public static final int HEARTBEAT_WAIT = 30000;
	
	private Logger log;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private BlockingQueue<WsResponse> queue;
	private boolean running = false;
	private final CountDownLatch closeLatch;
    private Session session;
 
    public WsSocketServer() {
    	log = Logger.getLogger(this.getClass());
        this.closeLatch = new CountDownLatch(1);
    }
 
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    	log.warn("Connection closed: ["+statusCode+"] - ["+reason+"]");
        this.session = null;
        this.closeLatch.countDown();
        stopWsSocketSerices();
    }
 
    @OnWebSocketConnect
    public void onConnect(Session session) {
    	log.info("Starting new wsSocketSession");
        this.session = session;
        this.session.setIdleTimeout(60000);
        sendMessage(makeConnectionAck());
    }
 
    @OnWebSocketMessage
    public void onMessage(String msg) {
    	log.debug("Incoming WsSocket Message <<<< "+msg);
        try{
        	WsRequest request = unmarshalRequest(msg);
        	wsCoordinator.runRequest(request, this);
        }catch(WsException e){
        	sendErrorMessage(e.getMessage());
        }
    }

	@Override
	public void onResponse(WsResponse response) {
		String r = marshalResponse(response);
        sendMessage(r);
        
        if(response.isSuccess() && response.getGeneralType().equals("DeviceSession")){
        	DeviceSession dSession = (DeviceSession) response.getResponse();
        	if(dSession != null && dSession.isActive()){
        		try {
					Device d = deviceCoordinator.getDeviceBySession(dSession);
					d.setConnectionType(ConnectionType.WSOCKET);
					deviceCoordinator.updateDevice(d);
					
					queue = socketCoordinator.register(this, d);
					if(queue != null){
						log.debug("Succsfully registered with coordinator. Starting sender service");
						startWsSocketSenderService(d);
						startWsSocketHeartbeatService(d);
					}
				} catch (DeviceException e) {
					log.error("Error getting device via session", e);
				}
        	}
        }
	}
	
	private String makeConnectionAck(){
		WsResponse r = new WsResponse();
		r.setSuccess(true);
		r.setSubSystem("WsSocket");
		
		return marshalResponse(r);
	}
	

	private void sendErrorMessage(String message) {
		WsResponse r = new WsResponse();
		r.setSuccess(false);
		r.setSubSystem("WsSocket");
		r.setErrorMessage(message);
		
		String res = marshalResponse(r);
		sendMessage(res);
	}
	
	private void sendMessage(String message){
		if(session != null){
			try {
				session.getRemote().sendString(message);
				log.debug("Sent WsSocket message >>>> " + message);
			} catch (IOException e) {
				log.error("Error sending response back to WsSocket client", e);
			}
		}
	}
	
	private String marshalResponse(WsResponse wsResponse) {
		if(marshaller == null){
			marshaller = JaxbFactory.getMarshaller();
		}
		
		StringWriter sw = new StringWriter();
		
		try{
			marshaller.marshal(wsResponse, sw);
		}catch(JAXBException je){
			log.error("Error marshalling wsResponse object ["+je.getMessage()+"]");
		}
		
		return sw.toString();
	}
	
	private WsRequest unmarshalRequest(String requestStr) throws WsException {
		if(unmarshaller == null){
			unmarshaller = JaxbFactory.getUnmarshaller();
		}
		
		StringReader sr = new StringReader(requestStr);
		try {
			Object obj = unmarshaller.unmarshal(sr);
			
			if(obj instanceof WsRequest){
				return (WsRequest) obj;
			}else{
				throw new WsException("Unknown object error");
			}
		} catch (JAXBException e) {
			log.error("Error unmarshalling WsRequest string", e);
			throw new WsException("Error unmarshalling WsRequest string");
		}
	}
	
	private void startWsSocketHeartbeatService(Device device){
		WsSocketHeartbeatService heartbeatService = new WsSocketHeartbeatService();
		running = true;
		heartbeatService.start();
		heartbeatService.setName("WsSocketHeartbeatService-"+device.getName());
	}

	private void startWsSocketSenderService(Device device){
		WsSocketSenderService senderService =  new WsSocketSenderService();
		running = true;
		senderService.start();
		senderService.setName("WsSocketSenderService-"+device.getName());
	}
	
	private void stopWsSocketSerices(){
		socketCoordinator.unregister(this);
		running = false;
	}
	
	class WsSocketSenderService extends Thread {
		public void run(){
			try{
				log.info("Initializing [" + this.getName() + "]");
				while(running){
					synchronized (queue) {
						if (!queue.isEmpty()){
							log.debug("Taking message from wsCallQueue");
							try{
								WsResponse message = queue.take();
								if(message != null && !message.equals("<E>")){
									sendMessage(marshalResponse(message));
								}
							}catch(Exception e){
								log.error("Error taking message from queue", e);
							}
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
			}catch(Exception e){
				log.error(e.getMessage(), e);
				onClose(0, e.getMessage());
			}
		}
	}
	
	class WsSocketHeartbeatService extends Thread {
		public void run(){
			try {
				log.info("Initializing [" + this.getName() + "]");
				while(running){
					// Send them every few mins
					synchronized (this) {
						sleep(HEARTBEAT_WAIT);
					}
					
					final WsResponse pingResponse = new WsResponse();
					pingResponse.setSubSystem("WsSocket");
					pingResponse.setResponse("Ping");
					pingResponse.setSuccess(true);
					
					// We don't really care about the response to from the remote device - the sockets in jetty will take care of the timeout
					synchronized (queue) {
						log.debug("Adding ping message to queue");
						queue.add(pingResponse);
					}
				}
			}catch(Exception e){
				onClose(0, e.getMessage());
			}
		}
	}
	
}
