package com.matt.remotr.core.device;

public class DeviceException extends Exception {

	private static final long serialVersionUID = 1L;

	public DeviceException(String message){
		super(message);
	}
	
	public DeviceException(String message, Throwable throwable){
		super(message, throwable);
	}

}
