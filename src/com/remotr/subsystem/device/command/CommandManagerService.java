package com.remotr.subsystem.device.command;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.remotr.subsystem.device.DeviceException;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.ws.WsBase;
import com.remotr.subsystem.ws.WsClass;
import com.remotr.subsystem.ws.WsCoordinator;
import com.remotr.subsystem.ws.WsMethod;
import com.remotr.subsystem.ws.WsParam;
import com.remotr.subsystem.ws.WsRunner;
import com.remotr.subsystem.ws.response.domain.WsResponse;

/**
 * WebService class for the command manager
 * @author matt
 *
 */
@WsClass(description="Manages commands on devices")
public class CommandManagerService extends WsBase implements WsRunner {

	private CommandManager commandManager;
	private WsCoordinator wsCoordinator;
	private Logger log;
	
	public CommandManagerService(){
		log = Logger.getLogger(this.getClass());
		subSystemName = "CommandManager";
	}
	
	@PostConstruct
	public void init(){
		wsCoordinator.register(this);
	}

	@WsMethod(
			isPublic=true,
			description="Trigger a command on device",
			wsParams = { 
					@WsParam(name="device", type=Device.class)
			})
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
	
	public void setCommandManager(CommandManager commandManager) {
		this.commandManager = commandManager;
	}

	public void setWsCoordinator(WsCoordinator wsCoordinator) {
		this.wsCoordinator = wsCoordinator;
	}

	@Override
	public String getSubSystemName() {
		return subSystemName;
	}

}
