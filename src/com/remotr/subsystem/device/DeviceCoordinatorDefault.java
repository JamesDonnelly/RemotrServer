package com.remotr.subsystem.device;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.remotr.core.hibernate.HibernateUtil;
import com.remotr.subsystem.device.argument.domain.Argument;
import com.remotr.subsystem.device.argument.jpa.ArgumentJPA;
import com.remotr.subsystem.device.command.domain.Command;
import com.remotr.subsystem.device.command.jpa.CommandJPA;
import com.remotr.subsystem.device.domain.ConnectionType;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.domain.DeviceType;
import com.remotr.subsystem.device.jpa.DeviceJPA;
import com.remotr.subsystem.device.resource.domain.Resource;
import com.remotr.subsystem.device.resource.jpa.ResourceJpa;
import com.remotr.subsystem.event.EventForwarder;
import com.remotr.subsystem.event.types.DeviceEvent;
import com.remotr.subsystem.event.types.EventType;
import com.remotr.subsystem.session.SessionCoordinator;
import com.remotr.subsystem.session.domain.DeviceSession;

/**
 * Default implementation of the device coordinator
 * @author mattm
 *
 */
// TODO: Fix issue where resources and commands are persisted twice due to update (being different)
// TODO: Set Ids on Command, Resource and Argument domain objects after persisting
public class DeviceCoordinatorDefault implements DeviceCoordinator {
	private DeviceList deviceCache; // THIS NEEDS TO BE SYNCONISED!
	private Logger log;
	private Session session;
	private EventForwarder eventForwarder;
	private SessionCoordinator sessionCoordinator;
	
	public DeviceCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		deviceCache = new DeviceList();
				
		try{
			// On startup, get any persisted device objects from the db
			log.info("Attempting to get persisted devices from database");
			session = HibernateUtil.getSessionFactory().openSession();
			Query queryResult = session.createQuery("from DeviceJPA");   
			List<?> tmpDevices = queryResult.list();
			for (int i = 0; i < tmpDevices.size(); i++) {  
				DeviceJPA deviceJpa = (DeviceJPA) tmpDevices.get(i);
				log.debug("Found device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] in db. Restoring");
				if(deviceCache.addByJPA(deviceJpa)){
					log.debug("Device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] restored");
					if(deviceJpa.getCommands() != null){
						log.debug("Device ["+deviceJpa.getName()+"] has ["+deviceJpa.getCommands().size()+"] commands registered");
					}
				}else{
					log.debug("Device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] failed to restore");
				}
			}
			log.info("Finished getting persisted devices from database");
		}catch(Exception e){
			log.error("Error restoring persisted devices", e);
			log.error("Panicing - Removing all devices from device list (List currently holds ["+deviceCache.size()+"] devices");
			deviceCache.removeAll();
		}finally{
			session.close();
		}
	}
	
	@PostConstruct
	public void init(){
		// Register the SYSTEM device
		Device d = new Device();
		d.setName("SYSTEM");
		d.setType(DeviceType.SYSTEM);
		d.setConnectionType(ConnectionType.NONE);
		
		Resource r = new Resource();
		r.setEventType(EventType.BROADCAST);
		r.setResourceName("SYSTEM-RESOURCE");
		
		ArrayList<Resource> rList = new ArrayList<Resource>();
		rList.add(r);
		d.setResources(rList);
		
		try {
			register(d);
		} catch (DeviceException e) {
			log.warn("Unable to register system device");
		}
	}

	@Override
	public DeviceSession register(Device device) throws DeviceException {	
		if(!validateDevice(device)){
			throw new DeviceException("Device registration failed: Device name or type can not be null");
		}
		log.info("Incoming request to register device ["+device.getName()+"]");
		DeviceJPA deviceJPA = new DeviceJPA(device);
		if(deviceCache.addByDevice(device)){
			Transaction transaction = null;
			
			try{
				session = HibernateUtil.getSessionFactory().openSession();
				transaction = session.beginTransaction();
				Long deviceId = (Long) session.save(deviceJPA);
				transaction.commit();
				device.setId(deviceId);
				
				Query queryDeviceById = session.getNamedQuery("device.getById");
				queryDeviceById.setLong("deviceId", deviceId);
				List<?> tmpDevices = queryDeviceById.list();
				for (int i = 0; i < tmpDevices.size(); i++) {  
					DeviceJPA deviceJpa = (DeviceJPA) tmpDevices.get(i);
					if(deviceJpa.getCommands() != null){
						for(CommandJPA cJpa : deviceJpa.getCommands()){
							for(Command c : device.getCommands()){
								if(c.getName() == cJpa.getName()){
									c.setId(cJpa.getId());
								}
								if(cJpa.getArguments() != null){
									for(ArgumentJPA aJpa: cJpa.getArguments()){
										for(Argument a : c.getArguments()){
											a.setId(aJpa.getId());
										}
									}
								}
							}
						}
					}
					if(deviceJpa.getResources() != null){
						for(ResourceJpa rJpa : deviceJpa.getResources()){
							for(Resource r : device.getResources()){
								r.setId(rJpa.getId());
							}
						}
					}
				}
				
				DeviceSession deviceSession = sessionCoordinator.login(device);
				device.setSessionKey(deviceSession.getSessionKey());
				
				log.info("Device ["+device.getName()+"] registered");
				triggerRecache();
		
				DeviceEvent event = createDeviceEvent("DeviceRegistered", device, EventType.DEVICE_REGISTER);
				eventForwarder.forwardEvent(event);
				
				return deviceSession;
			}catch(HibernateException he){
				log.error("Error persisting Device ["+device.getName()+"]", he);
				triggerRecache();
				throw new DeviceException("Error persisting Device ["+device.getName()+"]");
			}finally{
				if(session.isOpen()) session.close(); // Check first as it may be closed after calling updateDevice()
			}
		}else{
			String message = "Device ["+device.getName()+"] is already registered";
			log.warn(message);
			throw new DeviceException(message);
		}
	}

	@Override
	public boolean deregister(Device device) throws DeviceException {
		log.info("Incoming request to deregister device ["+device.getName()+"]");
		//session = HibernateUtil.getSessionFactory().openSession();
		
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			device = getDevice(device);
			if(device.getName().equals("SYSTEM")){
				throw new DeviceException("Can not remove system device ["+device.getName()+"]");
			}
			
			DeviceJPA deviceJPA = new DeviceJPA(device);
			
			session.beginTransaction();
			session.delete(deviceJPA);
			session.getTransaction().commit();
			
			sessionCoordinator.logout(device);
			deviceCache.removeByDevice(device);
			
			log.info("Device ["+device.getName()+"] deregistered");
			
			eventForwarder.forwardEvent(createDeviceEvent("DeviceDeregistered", device, EventType.DEVICE_UNREGISTER));
			
			return true;
		}catch(HibernateException he){
			log.error("Error removing persisted object", he);
			throw new DeviceException("Error removing persisted object");
		}finally{
			session.close();
		}
	}
	
	@Override
	public void updateDevice(Device device) throws DeviceException {
		if(!validateDevice(device)){
			throw new DeviceException("Device update failed: Device name or type can not be null");
		}
		if(checkDeviceRegistered(device)){
			log.info("Updating device ["+device.getName()+"]");
			deviceCache.replace(device);
			DeviceJPA deviceJPA = new DeviceJPA(device);
			try{
				session = HibernateUtil.getSessionFactory().openSession();
				session.beginTransaction();
				session.update(deviceJPA);
				session.getTransaction().commit();
				
				eventForwarder.forwardEvent(createDeviceEvent("DeviceUpdated", device, EventType.DEVICE_UPDATE));
				
			}catch(HibernateException he){
				log.error("Error persisting updated device ["+device.getName()+"]");
				throw new DeviceException("Error updating device");
			}finally{
				session.close();
			}
		}else{
			throw new DeviceException("Device ["+device.getName()+"] is not registered with this coordinator");
		}
	}

	@Override
	public boolean checkRegistered(String name, DeviceType type) {
		Device device = new Device();
		device.setName(name);
		device.setType(type);
		
		log.info("Incoming request to check status of device ["+device.getName()+"]");
		
		return(checkDeviceRegistered(device));
	}
	
	@Override
	public boolean checkDeviceRegistered(Device device) {
		if(deviceCache.contains(device)){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public Device getDevice(Device device) throws DeviceException {
		if(deviceCache.contains(device)){
			log.debug("Found device ["+device.getName()+"] - Returning this device");
			return deviceCache.getByTypes(device.getName(), device.getType());
		}else{
			String message = "Device ["+device.getName()+"] does not exist";
			log.warn(message);
			throw new DeviceException(message);
		}
	}
	
	@Override
	public Device getSystemDevice() throws DeviceException {
		Device d = new Device();
		d.setName("SYSTEM");
		d.setType(DeviceType.SYSTEM);
		
		d = getDevice(d);
		return d;
	}

	@Override
	public Device getDeviceByPos(int id) throws DeviceException {
		try{
			Device d = deviceCache.get(id);
			log.info("Found device ["+d.getName()+"] from position ["+id+"]");
			return d;
		}catch(IndexOutOfBoundsException e){
			String message = "Device with Id ["+id+"] does not exist";
			log.warn(message);
			throw new DeviceException(message);
		}
	}
	
	@Override
	public Device getDeviceById(Long id) throws DeviceException {
		Device d = deviceCache.get(id);
		if(d != null){
			return d;
		}else{
			String message = "Device with Id ["+id+"] does not exist";
			log.warn(message);
			throw new DeviceException(message);
		}
	}
	
	@Override
	public Device getDeviceBySession(DeviceSession dSession) throws DeviceException {
		return getDeviceBySessionKey(dSession.getSessionKey());
	}
	
	@Override
	public Device getDeviceBySessionKey(String sessionKey) throws DeviceException {
		if(sessionKey != null && sessionCoordinator.validate(sessionKey)){
			for(Device d : getAllRegisteredDevices()){
				if(d.getSessionKey() != null && d.getSessionKey().equals(sessionKey)){
					return d;
				}
			}
		}
		
		throw new DeviceException("Device not active or registered");
	}

	@Override
	public ArrayList<Device> getAllRegisteredDevices() {
		log.info("Incoming request to get all devices");
		if(deviceCache != null && deviceCache.size() > 0){
			return deviceCache.getDevices();
		}		
		return new ArrayList<Device>();
	}
	
	@Override
	public Long getHeartbeatTime(Device device) throws DeviceException{
		device = getDevice(device);
		return device.getLastHeartbeatTime();
	}
	
	@Override
	public void setHeartbeatTime(Device device) throws DeviceException{
		device = getDevice(device);
		device.setLastHeatbeatTime(System.currentTimeMillis());		
	}
	
	private void triggerRecache(){
		log.info("Device Coordinator recache triggered: Attempting to get persisted devices from database");
		try{
			session = HibernateUtil.getSessionFactory().openSession();
			Query queryResult = session.createQuery("from DeviceJPA");   
			List<?> tmpDevices = queryResult.list();
			deviceCache.removeAll();
			for (int i = 0; i < tmpDevices.size(); i++) {  
				DeviceJPA deviceJpa = (DeviceJPA) tmpDevices.get(i);
				log.debug("Found device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] in db. Restoring");
				if(deviceCache.addByJPA(deviceJpa)){
					log.debug("Device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] restored");
					log.debug("Device ["+deviceJpa.getName()+"] has ["+deviceJpa.getCommands().size()+"] commands registered");
				}else{
					log.debug("Device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] failed to restore");
				}
			}
			log.info("Finished getting persisted devices from database");
			
			DeviceEvent event = createDeviceEvent("DeviceRecache", this.getSystemDevice(), EventType.DEVICE_UPDATE);
			eventForwarder.forwardEvent(event);
			
		}catch(Exception e){
			log.error("Error restoring persisted devices", e);
			log.error("Panicing - Removing all devices from device list (List currently holds ["+deviceCache.size()+"] devices)");
			deviceCache.removeAll();
		}
	}
	
	private boolean validateDevice(Device device){
		if(device.getName() == null || device.getType() == null){
			return false;
		}else{
			return true;
		}
	}
	
	private DeviceEvent createDeviceEvent(String name, Device device, EventType eventType){
		DeviceEvent event = new DeviceEvent();
		event.setEventType(eventType);
		event.setName(name);
		event.setDeviceName(device.getName());
		event.setDeviceType(device.getType());
		
		return event;
	}
	
	protected String getSubsystem(){
		return "DeviceCoordinator";
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}

	public void setSessionCoordinator(SessionCoordinator sessionCoordinator) {
		this.sessionCoordinator = sessionCoordinator;
	}
}
