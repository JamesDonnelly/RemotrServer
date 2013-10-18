package com.remotr.subsystem.ws;

import static org.junit.Assert.*;

import java.util.ArrayList;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.domain.DeviceType;
import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsParamHolder;
import com.remotr.subsystem.ws.response.domain.WsResponse;
import com.remotr.subsystem.ws.response.domain.WsSubsystemResponse;

/**
 * Base class for test cases that need to test the service classes
 * @author matt
 *
 */
public abstract class WsTestBase {
	
	protected static String WSGET_SUBSYSTEMS = "getSubSystems";
	
	public WsCoordinator getWsCoordinator(){
		WsCoordinator wsCoordinator = new WsCoordinatorDefault();
		assertNotNull(wsCoordinator);
		assertNotNull(wsCoordinator.getSubSystemName());
		
		return wsCoordinator;
	}
	
	public WsRequest buildRequest(String subsystemName, String methodName, String reference){
		return buildRequest(subsystemName, methodName, null, reference, null);
	}
	
	public WsRequest buildRequest(String subsystemName, String methodName, String reference, String sessionKey){
		return buildRequest(subsystemName, methodName, null, reference, sessionKey);
	}
	
	public WsRequest buildRequest(String subsystemName, String methodName){
		return buildRequest(subsystemName, methodName, null, null, null);
	}
	
	public WsRequest buildRequest(String subsystemName, String methodName, ArrayList<WsParamHolder> params, 
			String reference, String sessionKey){
		WsRequest request = new WsRequest();
		
		// We are excepting these helper methods to work ;)
		request.setSubSystem(subsystemName);
		request.setMethod(methodName);
		if(params != null){
			for(WsParamHolder p : params){
				request.addParam(p.getName(), p.getValue());
			}
		}
		request.setReference(reference);
		request.setSessionKey(sessionKey);
		return request;
	}
	
	/**
	 * Tests the response object for basics based on the request. Assumes is should have been a success and was not a list response
	 */
	public void testResponse(WsRequest request, WsResponse response){
		testResponse(request, response, true, false);
	}
	
	/**
	 * Tests the response object for basics based on the request
	 */
	public void testResponse(WsRequest request, WsResponse response, boolean expectedSuccess, boolean expectedListResponse){
		assertNotNull(request);
		assertNotNull(response);
		
		assertNotNull(request.getMethod());
		assertNotNull(request.getSubSystem());
		
		assertEquals(expectedSuccess, response.isSuccess());
		
		if(!request.getReference().equals("") || request.getReference() != null){
			assertEquals(request.getReference(), response.getReference());
		}
		
		assertEquals(request.getSubSystem().getSubsystemName(), response.getSubSystem());

		if(response instanceof WsSubsystemResponse){
			WsSubsystemResponse r = (WsSubsystemResponse) response;
			assertNotNull(r.getSubsystemList());
		}
		else if(response instanceof WsResponse){
			if(expectedListResponse){
				assertNotNull(response.getListResponse());
			}else{
				assertNotNull(response.getResponse());
			}
		}
	}
	
	public Device getTestDevice(){
		return getTestDevice("TEST-DEVICE", DeviceType.SYSTEM);
	}
	
	public Device getTestDevice(String deviceName, DeviceType deviceType){
		Device d = new Device(deviceName, deviceType);
		return d;
	}

}
