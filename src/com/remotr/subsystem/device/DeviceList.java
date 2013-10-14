package com.remotr.subsystem.device;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import com.remotr.device.command.domain.Command;
import com.remotr.subsystem.device.argument.domain.Argument;
import com.remotr.subsystem.device.argument.jpa.ArgumentJPA;
import com.remotr.subsystem.device.command.jpa.CommandJPA;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.domain.DeviceType;
import com.remotr.subsystem.device.jpa.DeviceJPA;
import com.remotr.subsystem.device.resource.domain.Resource;
import com.remotr.subsystem.device.resource.jpa.ResourceJpa;

@XmlType(name="DeviceList")
class DeviceList {
	private ArrayList<Device> devices;
	
	public DeviceList(){
		devices = new ArrayList<Device>();
	}
	
	public boolean addByDevice(Device device){
		if(!contains(device)){
			return devices.add(device);
		}else{
			return false;
		}
	}
	
	public boolean addByJPA(DeviceJPA deviceJpa){
		Device device = new Device();
		device.setId(deviceJpa.getId());
		device.setName(deviceJpa.getName());
		device.setType(deviceJpa.getType());
		device.setConnectionType(deviceJpa.getConnectionType());
		device.setHasHeartbeat(deviceJpa.getHasHeartbeat());
		device.setLastHeatbeatTime(deviceJpa.getLastHeartbeatTime());
		
		// Convert from JPA to Domain for all types here
		if(deviceJpa.getCommands() != null){
			ArrayList<Command> cList = new ArrayList<Command>();
			for(CommandJPA cJpa : deviceJpa.getCommands()){
				cList.add(jpaToDomain(cJpa));
			}
			device.setCommands(cList);
		}
		
		if(deviceJpa.getResources() != null){
			ArrayList<Resource> rList = new ArrayList<Resource>();
			for(ResourceJpa rJpa : deviceJpa.getResources()){
				rList.add(jpaToDomain(rJpa));
			}
		}
		
		return addByDevice(device);
	}
	
	private Resource jpaToDomain(ResourceJpa rJpa) {
		Resource r = new Resource();
		r.setId(rJpa.getId());
		r.setEventType(rJpa.getEventType());
		r.setResourceName(rJpa.getResourceName());
		
		return r;
	}

	private Command jpaToDomain(CommandJPA jpa){
		Command c = new Command();
		c.setId(jpa.getId());
		c.setName(jpa.getName());
		c.setTrigger(jpa.getTrigger());
		
		if(jpa.getArguments() != null){
			ArrayList<Argument> aList = new ArrayList<Argument>();
			for(ArgumentJPA aJpa : jpa.getArguments()){
				aList.add(jpaToDomain(aJpa));
			}
			c.setArguments(aList);
		}
		
		return c;
	}
	
	private Argument jpaToDomain(ArgumentJPA jpa){
		Argument a = new Argument();
		a.setId(jpa.getId());
		a.setType(jpa.getType());
		a.setValue(jpa.getValue());
		
		return a;
	}

	public boolean addByTypes(String name, DeviceType type){
		Device device = new Device();
		device.setName(name);
		device.setType(type);
		device.setHasHeartbeat(false);
		device.setLastHeatbeatTime(0L);
		if(!devices.contains(device)){
			return devices.add(device);
		}else{
			return false;
		}
	}
	
	public boolean addByTypes(String name, DeviceType type, ArrayList<Command> commands){
		Device device = new Device();
		device.setName(name);
		device.setType(type);
		device.setHasHeartbeat(false);
		device.setLastHeatbeatTime(0L);
		device.setCommands(commands);
		if(!devices.contains(device)){
			return devices.add(device);
		}else{
			return false;
		}
	}
	
	public Device getByTypes(String name, DeviceType type){
		Device device = new Device();
		device.setName(name);
		device.setType(type);
		
		if(devices.contains(device)){
			int i = devices.indexOf(device);
			return devices.get(i);
		}else{
			return null;
		}
	}
	
	public boolean removeByTypes(String name, DeviceType type){
		Device device = new Device();
		device.setName(name);
		device.setType(type);
		device.setHasHeartbeat(false);
		if(devices.contains(device)){
			return devices.remove(device);
		}else{
			return false;
		}
	}
	
	public boolean removeByDevice(Device device){
		if(devices.contains(device)){
			return devices.remove(device);
		}else{
			return false;
		}
	}
	
	public void removeAll(){
		for(Device d : devices){
			devices.remove(d);
		}
	}
	
	public void replace(Device device){
		if(devices.contains(device)){
			int i = devices.indexOf(device);
			devices.set(i, device);
		}
	}
	
	@XmlElement(name="Device")
	@XmlElementWrapper(name="Devices")
	public ArrayList<Device> getDevices(){
		if(!devices.isEmpty()){
			return devices;
		}else{
			return null;
		}
	}
	
	public Device get(int id){
		return devices.get(id);
	}
	
	public boolean contains(Device device){
		return devices.contains(device);
	}
	
	public boolean isEmpty(){
		return devices.isEmpty();
	}
	
	public int size(){
		return devices.size();
	}
	
	/**
	 * Tad confusing, this is position of the device in the list
	 * @param device
	 * @return
	 */
	public int getDevicePos(Device device){
		return devices.indexOf(device);
	}
	
	/**
	 * Gets the device with the id
	 * @param device
	 * @return
	 */
	public Device get(Long id){
		for(Device d : devices){
			if(d.getId().equals(id)){
				return d;
			}
		}
		return null;
	}
	
}
