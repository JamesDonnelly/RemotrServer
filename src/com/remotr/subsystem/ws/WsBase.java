package com.remotr.subsystem.ws;

import com.remotr.core.Main;
import com.remotr.subsystem.ws.response.domain.WsResponse;

/**
 * Base abstract class for all classes exposing {@link WsMethod}s to extend
 * @author matt
 *
 */
public abstract class WsBase {
	
	protected String subSystemName;
	
	protected WsResponse getWsResponseForClass(){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(subSystemName);
		
		wsResponse.setVersionName(Main.getVersionName());
		wsResponse.setVersionNum(Main.getVersionNumber());
		
		return wsResponse;
	}
}
