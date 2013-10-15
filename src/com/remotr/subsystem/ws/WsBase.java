package com.remotr.subsystem.ws;

import org.apache.log4j.Logger;

import com.remotr.core.Main;
import com.remotr.subsystem.ws.response.domain.WsResponse;

/**
 * Base abstract class for all classes exposing {@link WsMethod}s to extend
 * @author matt
 *
 */
public abstract class WsBase {
	protected Logger log;
	protected String subSystemName;
	
	public WsBase(){
		log = Logger.getLogger(this.getClass());
	}
	
	protected WsResponse getWsResponseForClass(){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem(subSystemName);
		
		wsResponse.setVersionName(Main.getVersionName());
		wsResponse.setVersionNum(Main.getVersionNumber());
		
		return wsResponse;
	}
}
