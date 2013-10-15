package com.remotr.subsystem.session;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.session.domain.DeviceSession;


/**
 * Interface for the session coordinator. Implementing classes are responsible for managing and 
 * authenticating sessions throughout the system. 
 * @author matt
 *
 */
public interface SessionCoordinator {
	
	/**
	 * Starts a new session using the given {@link Device}. 
	 * Returns existing session if already logged in. 
	 * @param Device to create the session for
	 * @return active valid {@link DeviceSession}
	 */
	public DeviceSession login(Device device);
	
	/**
	 * Destroys the given {@link DeviceSession}
	 * @param Device that is logging out
	 * @return true if the {@link DeviceSession} was destroyed
	 */
	public boolean logout(Device device);
	
	/**
	 * Returns true if the given session key is part of an active and valid session
	 * @param sessionKey
	 * @return
	 */
	public boolean validate(String sessionKey);
	
	/**
	 * Returns a count of all currently active {@link DeviceSession}s
	 * @return
	 */
	public int getActionSessionCount();
	
}	
