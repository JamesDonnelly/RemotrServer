package com.matt.remotr.ws.request;

import com.matt.remotr.core.device.ConnectionType;
import com.matt.remotr.core.device.Device;

public interface WsRequestManager {
	
	/**
	 * Registers a {@link WsRequestRunner} with the {@link WsRequestManager}
	 * @param requestRunner
	 * @return
	 */
	public boolean register(WsRequestRunner requestRunner);
	
	/**
	 * Unregisters a {@link WsRequestRunner} from the {@link WsRequestManager}
	 * @param requestRunner
	 * @return
	 */
	public boolean unregister(WsRequestRunner requestRunner);
	
	/**
	 * Runs the given {@link WsRequest} and returns the result to the given device by the stated {@link ConnectionType}
	 * If no subsystem is found for the given request, then an exception response is sent to the device.
	 * It is recommended that the sender set the reference on the {@link WsRequest} before submitting. 
	 * @param wsRequest
	 * @param device
	 */
	public void runRequest(WsRequest wsRequest, Device device);

}
