package com.remotr.subsystem.device;

import java.util.ArrayList;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.domain.DeviceType;
import com.remotr.subsystem.session.domain.DeviceSession;

public interface DeviceCoordinator {
	
	public DeviceSession register(Device device) throws DeviceException;
	
	public boolean deregister(Device device) throws DeviceException;
	
	public boolean checkRegistered(String name, DeviceType type);
	
	public boolean checkDeviceRegistered(Device device);
	
	public Device getDevice(Device device) throws DeviceException;
	
	public Device getSystemDevice() throws DeviceException;
	
	public Device getDeviceByPos(int id) throws DeviceException;
	
	public Device getDeviceById(Long id) throws DeviceException;
	
	public Device getDeviceBySession(DeviceSession dSession) throws DeviceException;
	
	public Device getDeviceBySessionKey(String sessionKey) throws DeviceException;

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
