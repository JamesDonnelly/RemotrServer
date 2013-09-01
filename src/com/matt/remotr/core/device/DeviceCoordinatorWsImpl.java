package com.matt.remotr.core.device;

import javax.annotation.PostConstruct;
import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.device.domain.DeviceType;
import com.matt.remotr.ws.DeviceCoordinatorWs;
import com.matt.remotr.ws.request.WsRequestManager;
import com.matt.remotr.ws.response.domain.WsDeviceResponse;

@WebService(targetNamespace="http://remotr.org/wsdl", endpointInterface="com.matt.remotr.ws.DeviceCoordinatorWs", serviceName="device")
public class DeviceCoordinatorWsImpl extends SpringBeanAutowiringSupport implements DeviceCoordinatorWs {
	
	@Autowired
	private DeviceCoordinator deviceCoordinator;
	@Autowired
	private WsRequestManager requestManager;
	private Logger log;
	
	public DeviceCoordinatorWsImpl() {
		log = Logger.getLogger(getClass());
		log.info("Starting new DeviceCoordinator WebService");
	}
	
	@PostConstruct
	public void init(){
		requestManager.register(this);
	}

	@Override 
	public WsDeviceResponse register(Device device) {
		WsDeviceResponse wsDeviceResponse = new WsDeviceResponse();
		wsDeviceResponse.setSubSystem(getSubSystemName());
		try {
			wsDeviceResponse.setSuccess(deviceCoordinator.register(device));
		} catch (DeviceException e) {
			wsDeviceResponse.setErrorMessage(e.getMessage());
			log.error("Error during device register from webservice", e);
		}
		
		return wsDeviceResponse;
	}

	@Override
	public WsDeviceResponse deregister(Device device) {
		WsDeviceResponse wsDeviceResponse = new WsDeviceResponse();
		wsDeviceResponse.setSubSystem(getSubSystemName());
		try {
			wsDeviceResponse.setSuccess(deviceCoordinator.deregister(device));
		} catch (DeviceException e) {
			wsDeviceResponse.setErrorMessage(e.getMessage());
			log.error("Error during device register from webservice", e);
		}
		
		return wsDeviceResponse;
	}

	@Override
	public WsDeviceResponse checkRegistered(String name, DeviceType type) {
		WsDeviceResponse wsDeviceResponse = new WsDeviceResponse();
		wsDeviceResponse.setSubSystem(getSubSystemName());
		wsDeviceResponse.setSuccess(deviceCoordinator.checkRegistered(name, type));
		
		return wsDeviceResponse;
	}

	@Override
	public WsDeviceResponse getDeviceById(int id) {
		WsDeviceResponse wsDeviceResponse = new WsDeviceResponse();
		wsDeviceResponse.setSubSystem(getSubSystemName());
		try {
			wsDeviceResponse.setDeviceResponse((deviceCoordinator.getDeviceByPos(id)));
			wsDeviceResponse.setSuccess(true);
		} catch (DeviceException e) {
			wsDeviceResponse.setErrorMessage(e.getMessage());
			log.error("Error during device register from webservice", e);
		}
		
		return wsDeviceResponse;
	}

	@Override
	public WsDeviceResponse getAllRegisteredDevices() {
		WsDeviceResponse wsDeviceResponse = new WsDeviceResponse();
		wsDeviceResponse.setSubSystem(getSubSystemName());
		wsDeviceResponse.setDeviceListResponse(deviceCoordinator.getAllRegisteredDevices());
		wsDeviceResponse.setSuccess(true);
		
		return wsDeviceResponse;
	}

	@Override
	public String getSubSystemName() {
		return "DeviceCoordinator";
	}

}
