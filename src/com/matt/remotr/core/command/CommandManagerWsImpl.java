package com.matt.remotr.core.command;

import javax.jws.WebService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.matt.remotr.core.device.Device;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.ws.CommandManagerWs;
import com.matt.remotr.ws.response.WsResponse;

@WebService(targetNamespace="http://remotr.org/wsdl", endpointInterface="com.matt.remotr.ws.CommandManagerWs", serviceName="command")
public class CommandManagerWsImpl extends SpringBeanAutowiringSupport implements CommandManagerWs {
	
	@Autowired
	private CommandManager commandManager;
	private Logger log;
	
	public CommandManagerWsImpl(){
		log = Logger.getLogger(this.getClass());
	}

	@Override
	public WsResponse triggerCommand(Device device) {
		log.info("Incoming request to trigger command");
		WsResponse wsResponse = getWsResponseForClass();
		try {
			commandManager.triggerCommand(device);
		} catch (IllegalArgumentException e) {
			log.error("Error triggering command", e);
			wsResponse.setException(e);
		} catch (DeviceException e) {
			log.error("Error triggering command", e);
			wsResponse.setException(e);
		}
		return wsResponse;
	}
	
	private WsResponse getWsResponseForClass(){
		WsResponse wsResponse = new WsResponse();
		wsResponse.setSubSystem("CommandManager");
		return wsResponse;
	}

}
