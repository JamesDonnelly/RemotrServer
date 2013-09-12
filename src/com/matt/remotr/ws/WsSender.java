package com.matt.remotr.ws;

import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.ws.response.domain.WsResponse;

public interface WsSender {
	
	/**
	 * Send a {@link WsResponse} to a {@link Device}
	 * @param device
	 * @param wsResponse
	 * @return
	 */
	public Boolean sendMessage(Device device, WsResponse wsResponse);
	
	/**
	 * Send a {@link WsResponse} to all registered device servers
	 * @param wsResponse
	 * @return
	 */
	public void sendMessage(WsResponse wsResponse);

}