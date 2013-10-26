package com.remotr.subsystem.ws;


/**
 * Classes can implement interface to allow running of methods via the XMPP, TCPWS or Restful interface.
 * Not all methods can be run in RESTful way.
 * @author mattm
 *
 */
public interface WsRunner {
	
	public String getSubSystemName();
	
}
