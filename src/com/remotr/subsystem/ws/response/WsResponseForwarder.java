package com.remotr.subsystem.ws.response;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public interface WsResponseForwarder {
	
	public void forwardWsResponse(Device device, WsResponse wsResponse);

}
