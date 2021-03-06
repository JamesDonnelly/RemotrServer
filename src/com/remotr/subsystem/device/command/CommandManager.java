package com.remotr.subsystem.device.command;

import java.util.ArrayList;

import com.remotr.subsystem.device.DeviceException;
import com.remotr.subsystem.device.argument.domain.Argument;
import com.remotr.subsystem.device.command.domain.Command;
import com.remotr.subsystem.device.domain.Device;

/**
 * Interface for classes wanting to manage the commands on devices. 
 * @author mattm
 *
 */
public interface CommandManager {
	
	/**
	 * Trigger a command on the device. The {@link Command} to trigger should be wrapped in the {@link Device} with any required {@link Argument}
	 * This is the same as calling submitJob  then executeJob on the job subsystem
	 * @param device
	 * @return int - the job id
	 * @throws DeviceException 
	 * @throws IllegalArgumentException 
	 */
	public int triggerCommand(Device device) throws IllegalArgumentException, DeviceException;
	
	public void addCommand(Device device, Command command);
	
	public void removeCommand(Device device, Command command);
	
	public ArrayList<Command> getCommands(Device device);

}
