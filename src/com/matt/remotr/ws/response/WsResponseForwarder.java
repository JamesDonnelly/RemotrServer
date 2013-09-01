package com.matt.remotr.ws.response;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.ws.response.domain.WsResponse;

public interface WsResponseForwarder {
	
	public void forwardWsResponse(Device device, WsResponse wsResponse);

}
