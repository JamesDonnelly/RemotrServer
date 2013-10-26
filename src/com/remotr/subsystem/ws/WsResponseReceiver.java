package com.remotr.subsystem.ws;

import com.remotr.subsystem.ws.response.domain.WsResponse;

/**
 * Interface for receiving {@link WsResponse} objects from the WsCoordaintor in an async manor.
 * @author matt
 *
 */
public interface WsResponseReceiver {
	
	public void onResponse(WsResponse response);

}
