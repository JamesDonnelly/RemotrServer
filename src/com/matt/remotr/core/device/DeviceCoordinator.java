package com.matt.remotr.core.device;

import java.util.ArrayList;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.device.domain.DeviceType;

public interface DeviceCoordinator {
	
	public boolean register(Device device) throws DeviceException;
	
	public boolean deregister(Device device) throws DeviceException;
	
	public boolean checkRegistered(String name, DeviceType type);
	
	public boolean checkDeviceRegistered(Device device);
	
	public Device getDevice(Device device) throws DeviceException;
	
	public Device getSystemDevice() throws DeviceException;
	
	public Device getDeviceByPos(int id) throws DeviceException;
	
	public Device getDeviceById(Long id) throws DeviceException;

	public ArrayList<Device> getAllRegisteredDevices();
	
	@Deprecated
	/**
	 * Use updateDevice
	 */
	public Long getHeartbeatTime(Device device) throws DeviceException;
	
	@Deprecated
	/**
	 * Use updateDevice
	 */
	public void setHeartbeatTime(Device device) throws DeviceException;

	public void updateDevice(Device device) throws DeviceException;

}
