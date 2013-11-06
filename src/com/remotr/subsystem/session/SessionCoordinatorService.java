package com.remotr.subsystem.session;

import javax.annotation.PostConstruct;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.session.domain.DeviceSession;
import com.remotr.subsystem.ws.WsBase;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsRunner;
import com.remotr.subsystem.ws.annotations.WsClass;
import com.remotr.subsystem.ws.annotations.WsMethod;
import com.remotr.subsystem.ws.annotations.WsParam;
import com.remotr.subsystem.ws.response.domain.WsResponse;

@WsClass(description = "Provides methods for logging in and out of Remotr using a Device")
public class SessionCoordinatorService extends WsBase implements WsRunner {
	private WsCoordinator wsCoordinator;
	private SessionCoordinator sessionCoordinator;

	public SessionCoordinatorService(){
		super();
		subSystemName = "Session";
	}
	
	@PostConstruct
	public void init(){
		wsCoordinator.register(this);
	}
	
	@WsMethod(
			isPublic=true,
			description="Login using an existing device - The device MUST already be registered",
			wsParams = { 
					@WsParam(name="device", type=Device.class)
			})
	public WsResponse login(Device device){
		log.debug("Running login from Session wsService");
		WsResponse response = getWsResponse();
		DeviceSession deviceSession = sessionCoordinator.login(device);
		
		if(deviceSession != null){
			log.debug("Setting DeviceSession as response object");
			response.setResponse(deviceSession);
			response.setSuccess(true);
		}else{
			response.setErrorMessage("There was an error login in to Remotr");
		}
		
		return response;
	}
	
	public void setWsCoordinator(WsCoordinator wsCoordinator) {
		this.wsCoordinator = wsCoordinator;
	}

	public void setSessionCoordinator(SessionCoordinator sessionCoordinator) {
		this.sessionCoordinator = sessionCoordinator;
	}

	@Override
	public String getSubSystemName() {
		return subSystemName;
	}

}
