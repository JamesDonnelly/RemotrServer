package com.matt.remotr.core.command;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.device.DeviceCoordinatorDefault;
import com.matt.remotr.core.device.DeviceException;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.job.JobCoordinator;

/**
 * Manages the commands associated with the devices. Implements methods that can trigger commands when called
 * @author mattm
 *
 */
public class CommandManagerDefault extends DeviceCoordinatorDefault implements CommandManager {
	
	private Logger log;
	private JobCoordinator jobCoordinator;
	
	public CommandManagerDefault(){
		log = Logger.getLogger(this.getClass());
	}

	@Override
	public int triggerCommand(Device device) throws IllegalArgumentException, DeviceException {
		if(device.getCommands().size() > 1){
			throw new IllegalArgumentException("Triggering multiple commands is not supported by this manager");
		}
		
		Command tmpCommand = device.getCommands().get(0);
		Device tmpDevice = getDevice(device);
		int id = tmpDevice.getCommands().indexOf(tmpCommand);
		Command command = tmpDevice.getCommands().get(id);
		if(tmpCommand.getArguments().size() != command.getArguments().size()){
			throw new IllegalArgumentException("Not enough arguements given");
		}
		
		// Turn this in to a job... 
		// call submitJob
		// call execute job
		// return jobId
		
		return 0;
		
	}

	@Override
	public void addCommand(Device device, Command command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCommand(Device device, Command command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<Command> getCommands(Device device) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
