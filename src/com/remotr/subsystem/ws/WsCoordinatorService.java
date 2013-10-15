package com.remotr.subsystem.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.remotr.subsystem.ws.request.domain.WsRequest;
import com.remotr.subsystem.ws.response.domain.WsResponse;

@Path("ws")
public class WsCoordinatorService extends SpringBeanAutowiringSupport {
	
	@Autowired
	private WsCoordinator wsCoordinator;

	@POST
	@Produces(MediaType.TEXT_XML)
	@Consumes(MediaType.TEXT_XML)
	public WsResponse runRequest(WsRequest wsRequest) {
		wsRequest.setRest(true);
		return wsCoordinator.runRequest(wsRequest);
	}

	
	@GET
	@Produces(MediaType.TEXT_XML)
	public WsResponse getSubSystems() {
		WsRequest request = new WsRequest();
		
		request.setSubSystem(wsCoordinator.getSubSystemName());
		request.setMethod("getSubSystems");
		WsResponse response = wsCoordinator.runRequest(request);
		
		return response;
	}

}
