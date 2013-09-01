package com.matt.remotr.core.device;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.device.domain.ConnectionType;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.device.domain.DeviceType;
import com.matt.remotr.core.device.jpa.DeviceJPA;
import com.matt.remotr.core.event.EventForwarder;
import com.matt.remotr.core.event.types.DeviceEvent;
import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.main.hibernate.HibernateUtil;

/**
 * Default implementation of the device coordinator
 * @author mattm
 *
 */
public class DeviceCoordinatorDefault implements DeviceCoordinator {
	private DeviceList devices;
	private Logger log;
	private Session session;
	private EventForwarder eventForwarder;
	
	public DeviceCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		devices = new DeviceList();
				
		try{
			// On startup, get any persisted device objects from the db
			log.info("Attempting to get persisted devices from database");
			session = HibernateUtil.getSessionFactory().openSession();
			Query queryResult = session.createQuery("from DeviceJPA");   
			List<?> tmpDevices = queryResult.list();
			for (int i = 0; i < tmpDevices.size(); i++) {  
				DeviceJPA deviceJpa = (DeviceJPA) tmpDevices.get(i);
				log.debug("Found device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] in db. Restoring");
				if(devices.addByJPA(deviceJpa)){
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
			log.error("Panicing - Removing all devices from device list (List currently holds ["+devices.size()+"] devices");
			devices.removeAll();
		}finally{
			session.close();
		}
		
		// Register the SYSTEM device
		Device d = new Device();
		d.setName("SYSTEM");
		d.setType(DeviceType.SYSTEM);
		d.setConnectionType(ConnectionType.NONE);
		try {
			register(d);
		} catch (DeviceException e) {
			log.warn("Unable to register system device");
		}
	}

	@Override
	public boolean register(Device device) throws DeviceException {		
		log.info("Incoming request to register device ["+device.getName()+"]");
		DeviceJPA deviceJPA = new DeviceJPA(device);
		if(devices.addByDevice(device)){
			Transaction transaction = null;
			
			try{
				session = HibernateUtil.getSessionFactory().openSession();
				transaction = session.beginTransaction();
				Long deviceId = (Long) session.save(deviceJPA);
				device.setId(deviceId);
				if(device.getCommands() != null){
					for(Command c : device.getCommands()){ // TODO: fix npe here when a register device comes in from the TcpWs
						c.setDeviceId(deviceId);
					}
				}
				transaction.commit();
				log.info("Device ["+device.getName()+"] registered");
				
				// Update now as we have set the correct Ids on the Device
				updateDevice(device);
				
				DeviceEvent event = new DeviceEvent();
				event.setEventType(EventType.DEVICE_REGISTER);
				event.setName("DeviceRegistered");
				event.setDeviceName(device.getName());
				event.setDeviceType(device.getType());
				eventForwarder.forwardEvent(event, null);
				return true;
			}catch(HibernateException he){
				log.error("Error persisting Device ["+device.getName()+"]", he);
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
			devices.removeByDevice(device);
			log.info("Device ["+device.getName()+"] deregistered");
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
		if(checkDeviceRegistered(device)){
			log.info("Updating device ["+device.getName()+"]");
			devices.replace(device);
			DeviceJPA deviceJPA = new DeviceJPA(device);
			try{
				session = HibernateUtil.getSessionFactory().openSession();
				session.beginTransaction();
				session.update(deviceJPA);
				session.getTransaction().commit();
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
		if(devices.contains(device)){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public Device getDevice(Device device) throws DeviceException {
		if(devices.contains(device)){
			log.debug("Found device ["+device.getName()+"] - Returning this device");
			return devices.getByTypes(device.getName(), device.getType());
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
			Device d = devices.get(id);
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
		Device d = devices.get(id);
		if(d != null){
			return d;
		}else{
			String message = "Device with Id ["+id+"] does not exist";
			log.warn(message);
			throw new DeviceException(message);
		}
	}

	@Override
	public ArrayList<Device> getAllRegisteredDevices() {
		log.info("Incoming request to get all devices");
		if(devices != null || devices.size() > 0){
			return devices.getDevices();
		}		
		return null;
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
			devices.removeAll();
			for (int i = 0; i < tmpDevices.size(); i++) {  
				DeviceJPA deviceJpa = (DeviceJPA) tmpDevices.get(i);
				log.debug("Found device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] in db. Restoring");
				if(devices.addByJPA(deviceJpa)){
					log.debug("Device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] restored");
					log.debug("Device ["+deviceJpa.getName()+"] has ["+deviceJpa.getCommands().size()+"] commands registered");
				}else{
					log.debug("Device ["+deviceJpa.getId()+"]["+deviceJpa.getName()+"] failed to restore");
				}
			}
			log.info("Finished getting persisted devices from database");
		}catch(Exception e){
			log.error("Error restoring persisted devices", e);
			log.error("Panicing - Removing all devices from device list (List currently holds ["+devices.size()+"] devices");
			devices.removeAll();
		}
	}
	
	protected String getSubsystem(){
		return "DeviceCoordinator";
	}

	public EventForwarder getEventForwarder() {
		return eventForwarder;
	}

	public void setEventForwarder(EventForwarder eventForwarder) {
		this.eventForwarder = eventForwarder;
	}
}
