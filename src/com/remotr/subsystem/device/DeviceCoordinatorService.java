package com.remotr.subsystem.device;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.remotr.core.Main;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.domain.DeviceType;
import com.remotr.subsystem.session.domain.DeviceSession;
import com.remotr.subsystem.ws.WsBase;
import com.remotr.subsystem.ws.WsClass;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsMethod;
import com.remotr.subsystem.ws.WsParam;
import com.remotr.subsystem.ws.WsRunner;
import com.remotr.subsystem.ws.response.domain.WsDeviceResponse;

//TODO: Anything modifying devices should check the session key first

@WsClass(description="Handles all device services within Remotr")
public class DeviceCoordinatorService extends WsBase implements WsRunner {
	
	private DeviceCoordinator deviceCoordinator;
	private WsCoordinator wsCoordinator;
	private Logger log;
	
	public DeviceCoordinatorService() {
		log = Logger.getLogger(getClass());
		subSystemName = "Device";
		log.info("Starting new DeviceCoordinator WebService");
	}
	
	@PostConstruct
	public void init(){
		wsCoordinator.register(this);
	}

	@WsMethod(
			isPublic=true,
			description="Register a device",
			wsParams = { 
					@WsParam(name="device", type=Device.class)
			})
	public WsDeviceResponse register(Device device) {
		WsDeviceResponse wsDeviceResponse = getDeviceResponseForClass();
		try {
			DeviceSession ds = deviceCoordinator.register(device);
			wsDeviceResponse.setResponse(ds);
			wsDeviceResponse.setSuccess(true);
		} catch (DeviceException e) {
			wsDeviceResponse.setErrorMessage(e.getMessage());
			log.error("Error during device register from webservice", e);
		}
		
		return wsDeviceResponse;
	}

	@WsMethod(
			isPublic=false,
			description="Unregister a device",
			wsParams = { 
					@WsParam(name="device", type=Device.class)
			})
	public WsDeviceResponse deregister(Device device) {
		WsDeviceResponse wsDeviceResponse = getDeviceResponseForClass();
		try {
			wsDeviceResponse.setSuccess(deviceCoordinator.deregister(device));
		} catch (DeviceException e) {
			wsDeviceResponse.setErrorMessage(e.getMessage());
			log.error("Error during device register from webservice", e);
		}
		
		return wsDeviceResponse;
	}

	@WsMethod(
			isPublic=true,
			description="Check a device is registered based on the devices name and type",
			wsParams = { 
					@WsParam(name="name", type=String.class),
					@WsParam(name="type", type=DeviceType.class)
			})
	public WsDeviceResponse checkRegistered(String name, DeviceType type) {
		WsDeviceResponse wsDeviceResponse = getDeviceResponseForClass();
		wsDeviceResponse.setSuccess(deviceCoordinator.checkRegistered(name, type));
		
		return wsDeviceResponse;
	}

	@WsMethod(
			isPublic=false,
			description="Get a device by it's ID",
			wsParams = { 
					@WsParam(name="id", type=Integer.class),
			})
	public WsDeviceResponse getDeviceById(int id) {
		WsDeviceResponse wsDeviceResponse = getDeviceResponseForClass();
		try {
			wsDeviceResponse.setDeviceResponse((deviceCoordinator.getDeviceByPos(id)));
			wsDeviceResponse.setSuccess(true);
		} catch (DeviceException e) {
			wsDeviceResponse.setErrorMessage(e.getMessage());
			log.error("Error during device register from webservice", e);
		}
		
		return wsDeviceResponse;
	}

	@WsMethod(
			isPublic=false,
			description="Get all the registered devices"
			)
	public WsDeviceResponse getAllRegisteredDevices() {
		WsDeviceResponse wsDeviceResponse = getDeviceResponseForClass();
		wsDeviceResponse.setDeviceListResponse(deviceCoordinator.getAllRegisteredDevices());
		wsDeviceResponse.setSuccess(true);
		
		return wsDeviceResponse;
	}
	
	private WsDeviceResponse getDeviceResponseForClass(){
		WsDeviceResponse wsDeviceResponse = new WsDeviceResponse();
		wsDeviceResponse.setSubSystem(getSubSystemName());
		wsDeviceResponse.setVersionName(Main.getVersionName());
		wsDeviceResponse.setVersionNum(Main.getVersionNumber());
		
		return wsDeviceResponse;
	}

	@Override
	public String getSubSystemName() {
		return subSystemName;
	}

	public void setDeviceCoordinator(DeviceCoordinator deviceCoordinator) {
		this.deviceCoordinator = deviceCoordinator;
	}

	public void setWsCoordinator(WsCoordinator wsCoordinator) {
		this.wsCoordinator = wsCoordinator;
	}

}
