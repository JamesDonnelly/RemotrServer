package com.remotr.subsystem.websocket;

import java.util.concurrent.BlockingQueue;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.WsSender;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public interface WsSockectCoordinator extends WsSender {
	
	public BlockingQueue<WsResponse> register(WsSocketServer wsSockectServer, Device device);
	
	public void unregister(WsSocketServer wsSocketServer);
	
}
