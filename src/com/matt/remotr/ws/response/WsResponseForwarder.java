package com.matt.remotr.ws.response;

import com.matt.remotr.core.device.Device;

public interface WsResponseForwarder {
	
	public void forwardWsResponse(Device device, WsResponse wsResponse);

}
