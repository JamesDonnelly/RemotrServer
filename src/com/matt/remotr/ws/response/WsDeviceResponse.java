package com.matt.remotr.ws.response;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.matt.remotr.core.device.Device;

/**
 * Wrapper class for exporting device objects via the web service
 * @author mattm
 *
 */

@XmlRootElement
public class WsDeviceResponse extends WsResponse {
	
	private ArrayList<Device> deviceList;
	private Device device;
	private Boolean exists;
	
	
	@XmlElement(name="Device")
	@XmlElementWrapper(name="DeviceList")
	public ArrayList<Device> getDeviceListResponse() {
		return deviceList;
	}
	
	@XmlElement(name="Device")
	public Device getDeviceResponse() {
		return device;
	}
	
	public void setDeviceListResponse(ArrayList<Device> deviceList) {
		this.deviceList = deviceList;
	}
	
	public void setDeviceResponse(Device device) {
		this.device = device;
	}

	@XmlElement(name="DeviceExists")
	public Boolean getExists() {
		return exists;
	}

	public void setExists(Boolean exists) {
		this.exists = exists;
	}

}
