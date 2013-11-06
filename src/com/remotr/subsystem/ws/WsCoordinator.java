package com.remotr.subsystem.ws;

import java.util.ArrayList;

import com.remotr.subsystem.device.domain.ConnectionType;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsResponse;
import com.remotr.subsystem.ws.response.domain.WsSubsystemHolder;

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
	public void runRequest(final WsRequest wsRequest, final Device device);
	
	/**
	 * Runs the given {@link WsRequest} and returns the result via the {@link WsResponseReceiver}
	 * @param wsRequest
	 * @param responseReceiver
	 */
	public void runRequest(final WsRequest wsRequest, final WsResponseReceiver responseReceiver);
	
	/**
	 * Runs the given {@link WsRequest} and returns the result. 
	 * @param wsRequest
	 * @return {@link WsResponse}
	 */
	public WsResponse runRequest(WsRequest wsRequest);
	
	/**
	 * Returns a list of the subsystems and their methods
	 * @return
	 */
	public ArrayList<WsSubsystemHolder> getSubSystemList();

}
