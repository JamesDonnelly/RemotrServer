package com.matt.remotr.core.device;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.matt.remotr.core.command.Command;
import com.matt.remotr.main.hibernate.HibernateUtil;

// TODO: Implement cascade deletes
/**
 * Default implementation of the device coordinator
 * @author mattm
 *
 */
public class DeviceCoordinatorDefault implements DeviceCoordinator {
	private DeviceList devices;
	private Logger log;
	private Session session;
	
	public DeviceCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		devices = new DeviceList();
		
		try{
			// On startup, get any persisted device objects from the db
			log.info("Attempting to get persisted devices from database");
			session = HibernateUtil.getSessionFactory().openSession();
			Query queryResult = session.createQuery("from Device");   
			List<?> tmpDevices = queryResult.list();
			for (int i = 0; i < tmpDevices.size(); i++) {  
				Device device = (Device) tmpDevices.get(i);
				log.debug("Found device ["+device.getId()+"]["+device.getName()+"] in db. Restoring");
				if(devices.addByDevice(device)){
					log.debug("Device ["+device.getId()+"]["+device.getName()+"] restored");
					if(device.getCommands() != null){
						log.debug("Device ["+device.getName()+"] has ["+device.getCommands().size()+"] commands registered");
					}
				}else{
					log.debug("Device ["+device.getId()+"]["+device.getName()+"] failed to restore");
				}
			}
			log.info("Finished getting persisted devices from database");
		}catch(Exception e){
			log.error("Error restoring persisted devices", e);
			log.error("Panicing - Removing all devices from device list (List currently holds ["+devices.size()+"] devices");
			devices.removeAll();
		}
	}

	@Override
	public boolean register(Device device) throws DeviceException {		
		log.info("Incoming request to register device ["+device.getName()+"]");
		if(devices.addByDevice(device)){
			session = HibernateUtil.getSessionFactory().openSession();
			Transaction transaction = null;
			
			try{
				transaction = session.beginTransaction();
				Long deviceId = (Long) session.save(device);
				device.setId(deviceId);
				for(Command c : device.getCommands()){
					c.setDeviceId(deviceId);
				}
				transaction.commit();
				log.info("Device ["+device.getName()+"] registered");
				return true;
			}catch(HibernateException he){
				log.error("Error persisting Device ["+device.getName()+"]", he);
				throw new DeviceException("Error persisting Device ["+device.getName()+"]");
			}finally{
				session.close();
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
		session = HibernateUtil.getSessionFactory().openSession();
		
		try{
			session.beginTransaction();
			session.delete(device);
			session.getTransaction().commit();
			devices.removeByDevice(device);
			log.info("Device ["+device.getName()+"] deregistered");
			return true;
		}catch(HibernateException he){
			log.error("Error removing persisted object", he);
			throw new DeviceException("Error removing persisted object");
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
			Query queryResult = session.createQuery("from Device");   
			List<?> tmpDevices = queryResult.list();
			devices.removeAll();
			for (int i = 0; i < tmpDevices.size(); i++) {  
				Device device = (Device) tmpDevices.get(i);
				log.debug("Found device ["+device.getId()+"]["+device.getName()+"] in db. Restoring");
				if(devices.addByDevice(device)){
					log.debug("Device ["+device.getId()+"]["+device.getName()+"] restored");
					log.debug("Device ["+device.getName()+"] has ["+device.getCommands().size()+"] commands registered");
				}else{
					log.debug("Device ["+device.getId()+"]["+device.getName()+"] failed to restore");
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
	
}
