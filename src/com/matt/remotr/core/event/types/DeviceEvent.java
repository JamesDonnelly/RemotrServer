package com.matt.remotr.core.event.types;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.matt.remotr.core.device.DeviceType;

@XmlRootElement
public class DeviceEvent extends Event {
	private String deviceName;
	private DeviceType deviceType;
	
	@XmlElement(name="DeviceName")
	public String getDeviceName() {
		return deviceName;
	}
	
	@XmlElement(name="DeviceType")
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
}
