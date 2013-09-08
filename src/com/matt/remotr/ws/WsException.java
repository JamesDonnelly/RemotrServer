package com.matt.remotr.ws;

public class WsException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public WsException (){
		super ();
	}
	
	public WsException(String message){
		super (message);
	}

}
