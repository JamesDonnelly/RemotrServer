package com.matt.remotr.core.device;

import java.util.ArrayList;

public interface DeviceCoordinator {
	
	public boolean register(Device device) throws DeviceException;
	
	public boolean deregister(Device device) throws DeviceException;
	
	public boolean checkRegistered(String name, DeviceType type);
	
	public boolean checkDeviceRegistered(Device device);
	
	public Device getDevice(Device device) throws DeviceException;
	
	public Device getDeviceByPos(int id) throws DeviceException;
	
	public Device getDeviceById(Long id) throws DeviceException;

	public ArrayList<Device> getAllRegisteredDevices();
	
	public Long getHeartbeatTime(Device device) throws DeviceException;
	
	public void setHeartbeatTime(Device device) throws DeviceException;

}
