package com.remotr.subsystem.websocket;

import java.util.concurrent.BlockingQueue;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.WsEndpointProvider;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public interface WsSockectManager extends WsEndpointProvider {
	
	public BlockingQueue<WsResponse> register(WsSocketServer wsSockectServer, Device device);
	
	public void unregister(WsSocketServer wsSocketServer);
	
}
