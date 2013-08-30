package com.matt.remotr.xmpp;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import com.matt.remotr.core.device.ConnectionType;
import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.device.DeviceCoordinator;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.event.EventCoordinator;
import com.matt.remotr.core.event.EventReceiver;
import com.matt.remotr.core.event.EventType;
import com.matt.remotr.core.event.types.DeviceEvent;
import com.matt.remotr.core.event.types.Event;
import com.matt.remotr.main.jaxb.JaxbFactory;
import com.matt.remotr.ws.response.WsResponse;
/**
 * The default implementation of the {@link XmppCoordinator}. 
 * This is a little different from the TcpWs. XMPP supports the WsRequest class that can be used to call the SOAP service, it also supports 'offline' messages. 
 * Events sent to a device when it doesn't have a sender thread running is still sent, but it's up to the XMPP server to cache the message
 * @author mattm
 *
 */
public class XmppCoordinatorDefault implements XmppCoordinator, EventReceiver {
	private Logger log;
	private Marshaller marshaller = null;
	
	private XMPPConnection connection;
	private ChatManager chatManager;
	private Roster roster;
	
	private DeviceCoordinator deviceCoordinator;
	private EventCoordinator eventCoordinator;
	
	protected ArrayList<XmppMessageManager> messageManagers;
	protected Map<XmppMessageManager, Device> messageManagerDevice;
	protected Map<XmppMessageManager, BlockingQueue<String>> messageManagerQueue;

	public XmppCoordinatorDefault(String xmppServerAddress, DeviceCoordinator deviceCoordinator){
		log = Logger.getLogger(this.getClass());
		log.info("Starting XMPP Connection");
		this.deviceCoordinator = deviceCoordinator; 
		
		messageManagers = new ArrayList<XmppMessageManager>();
		messageManagerDevice = new HashMap<XmppMessageManager, Device>();
		messageManagerQueue = new HashMap<XmppMessageManager, BlockingQueue<String>>();
		
		ConnectionConfiguration config = new ConnectionConfiguration(xmppServerAddress);
		config.setSendPresence(true);
		config.setRosterLoadedAtLogin(true);
		connection = new XMPPConnection(config);
		
		try {
			connection.connect();
			connection.login("remotrServer", "remotrServer");
			
			// Get the Chat Manager
			chatManager = connection.getChatManager();
			chatManager.addChatListener(new XmppDeviceListener(this, deviceCoordinator));
			
			// Get the Chat Roster
			roster = connection.getRoster();
			roster.setSubscriptionMode(SubscriptionMode.accept_all);
			roster.addRosterListener(new XmppRosterManager(this));
			
			// Set the presence for us
			Presence presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);

		} catch (XMPPException e) {
			log.error("Error when connecting to XMPP Server", e);
			// Some sort of retry thread here
		}
		
		marshaller = JaxbFactory.getMarshaller();
	}
	
	@PostConstruct
	private void initRoster(){
		if(roster != null && deviceCoordinator != null){
			ArrayList<Device> deviceListCache = deviceCoordinator.getAllRegisteredDevices();
			for(Device d : deviceListCache){
				if(d.getConnectionType() == ConnectionType.XMPP) {
					try {
						log.debug("Adding roster entry for ["+d.getName()+"]");
						roster.createEntry(d.getName()+"@sancho", d.getName(), null);
						Presence subscribe = new Presence(Presence.Type.subscribe);
						subscribe.setTo(d.getName());
						connection.sendPacket(subscribe);
					} catch (XMPPException e) {
						log.warn("Can't add device ["+d.getName()+"] to roster - messages may be missed");
					}
				}
			}
		}
		registerForEvents();
	}
	
	private void registerForEvents(){
		if(eventCoordinator != null){
			eventCoordinator.registerForEvents(this);
		}
	}
	
	public void setEventCoordinator(EventCoordinator eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}

	@Override
	public void handleEvent(Device device, Event event) {
		eventCoordinator.forwardEvent(event, device);
	}
	
	@Override
	public void sendMessage(WsResponse wsResponse) {
		log.debug("Adding WsResponse from ["+wsResponse.getSubSystem()+"] to queue for all devices");
		synchronized (messageManagerQueue) {
			try{
				StringWriter sw = new StringWriter();
				marshaller.marshal(wsResponse, sw);
				for(BlockingQueue<String> bq : messageManagerQueue.values()){
					bq.add(sw.toString());
				}
			}catch(JAXBException je){
				log.error("JAXB Error when marshalling WsResponse", je);
			}
		}	
	}
	
	@Override
	public Boolean sendMessage(Device device, WsResponse wsResponse) {
		log.debug("Adding WsResponse from ["+wsResponse.getSubSystem()+"] to queue for device ["+device.getName()+"]");
		synchronized (messageManagerDevice) {
			try{
				if(messageManagerDevice.containsValue(device)){
					StringWriter sw = new StringWriter();
					marshaller.marshal(wsResponse, sw);
					XmppMessageManager manager = getMessageManagerForDevice(device);
					if(manager != null){
						synchronized (messageManagerQueue) {
							BlockingQueue<String> queue = messageManagerQueue.get(manager);
							queue.add(sw.toString());
							return true;
						}
					}
				}else{
					log.warn("No messageManager found for device ["+device.getName()+"]");
					if(wsResponse.getResponse() instanceof Event && roster.contains(device.getName())){
						log.warn("Attempting to send offline event to ["+device.getName()+"]");
						Chat chat = chatManager.createChat(device.getName(), new XmppMessageManager(this, deviceCoordinator));
						
						StringWriter sw = new StringWriter();
						marshaller.marshal(wsResponse, sw);
						
						Message message = new Message();
						message.setBody(sw.toString());
						message.setFrom("remotrServer");
						chat.sendMessage(message);
						
						return true;
					}
				}
			}catch(JAXBException je){
				log.error("JAXB Error when marshalling WsResponse", je);
			} catch (XMPPException e) {
				log.error("Error sending offline message to ["+device.getName()+"] - This response has been lost");
			}
		}
		return false;
	}

	@Override
	public BlockingQueue<String> register(XmppMessageManager messageManager, Device device) {
		synchronized (messageManagerDevice) {
			if(!messageManagerDevice.containsKey(messageManager)){
				messageManagerDevice.put(messageManager, device);
				
				synchronized (messageManagerQueue) {
					BlockingQueue<String> queue = new LinkedBlockingQueue<String>(20);
					messageManagerQueue.put(messageManager, queue);
					try {
						roster.createEntry(device.getName()+"@sancho", device.getName(), null);
						roster.addRosterListener(messageManager);
						Presence subscribe = new Presence(Presence.Type.subscribe);
						subscribe.setTo(device.getName());
						connection.sendPacket(subscribe);
					} catch (XMPPException e) {
						log.warn("Can't add device to roster - messages may be missed");
					}
					return queue;
				}
			}else{
				return null;
			}
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void unregister(XmppMessageManager messageManager) {
		synchronized (messageManagerDevice) {
			if(messageManagerDevice.containsKey(messageManager)){
				log.debug("Unregistering messageManager ["+messageManager.toString()+"] from XmppCoordinator");
				messageManagerDevice.remove(messageManager);
				messageManager.connectionsCount--;
			}
		}
		
		synchronized (messageManagerQueue) {
			if(messageManagerQueue.containsKey(messageManager)){
				messageManagerQueue.remove(messageManager);
			}
		}
		
		roster.removeRosterListener(messageManager);
	}

	@Override
	public void sendPing(Device device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sayHello(Device device) {
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(this.getClass().getSimpleName());
		wsResponse.setSuccess(true);
		
		sendMessage(device, wsResponse);
	}
	
	private XmppMessageManager getMessageManagerForDevice(Device device){
		synchronized (messageManagerDevice) {
			for(Entry<XmppMessageManager, Device> entry : messageManagerDevice.entrySet()){
				if(device.equals(entry.getValue())){
					return entry.getKey();
				}
			}
		}
		return null;
	}
	
	@Override
	public void onBroadcastEvent(Event event) {}

	@Override
	public void onEvent(Event event) {
		log.debug("Recieved event of type ["+event.getEventType().toString()+"]");
		if(event.getEventType() == EventType.DEVICE_UNREGISTER){
			DeviceEvent deviceEvent = (DeviceEvent) event;
			
			Device device = new Device();
			device.setName(deviceEvent.getDeviceName());
			device.setType(deviceEvent.getDeviceType());
			
			try {
				device = deviceCoordinator.getDevice(device);
				XmppMessageManager manager = getMessageManagerForDevice(device);
				if(manager != null){
					synchronized (messageManagerQueue) {
						BlockingQueue<String> queue = messageManagerQueue.get(manager);
						queue.add("<E>");
					}
				}
			} catch (DeviceException e) {
				
			}
			
			
		}
	}

}
