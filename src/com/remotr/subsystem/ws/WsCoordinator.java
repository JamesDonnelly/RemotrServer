package com.remotr.subsystem.ws;

import com.remotr.subsystem.device.domain.ConnectionType;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsResponse;

public interface WsCoordinator {
	
	/**
	 * Registers a {@link WsRunner} with the {@link WsCoordinator}
	 * @param requestRunner
	 * @return
	 */
	public boolean register(WsRunner requestRunner);
	
	/**
	 * Unregisters a {@link WsRunner} from the {@link WsCoordinator}
	 * @param requestRunner
	 * @return
	 */
	public boolean unregister(WsRunner requestRunner);
	
	/**
	 * Runs the given {@link WsRequest} and returns the result to the given device by the stated {@link ConnectionType}
	 * If no subsystem is found for the given request, then an exception response is sent to the device.
	 * It is recommended that the sender set the reference on the {@link WsRequest} before submitting. 
	 * @param wsRequest
	 * @param device
	 */
	public void runRequest(WsRequest wsRequest, Device device);
	
	
	/**
	 * Runs the given {@link WsRequest} and returns the result. 
	 * @param wsRequest
	 * @return {@link WsResponse}
	 */
	public WsResponse runRequest(WsRequest wsRequest);
	
	/**
	 * Returns the WsCoordinators internal subsystem name
	 */
	public String getSubSystemName();

}
