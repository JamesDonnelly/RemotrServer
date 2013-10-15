package com.remotr.subsystem.session;

import com.remotr.subsystem.ws.WsBase;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsRunner;

public class SessionCoordinatorService extends WsBase implements WsRunner {
	private WsCoordinator wsCoordinator;
	private SessionCoordinator sessionCoordinator;

	public SessionCoordinatorService(){
		super();
		subSystemName = "Session";
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
