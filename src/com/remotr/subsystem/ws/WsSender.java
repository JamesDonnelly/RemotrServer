package com.remotr.subsystem.ws;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public interface WsSender {
	
	/**
	 * Send a {@link WsResponse} to a {@link Device}
	 * @param device
	 * @param wsResponse
	 * @return
	 */
	public Boolean sendResponse(Device device, WsResponse wsResponse);
	
	/**
	 * Send a {@link WsResponse} to all registered device servers
	 * @param wsResponse
	 * @return
	 */
	public void sendResponse(WsResponse wsResponse);

}
