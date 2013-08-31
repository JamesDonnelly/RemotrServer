package com.matt.remotr.ws.request;


/**
 * Classes can implement interface to allow running of SOAP WeService methods via the other interfaces (XMPP, TCPWS)
 * @author mattm
 *
 */
public interface WsRequestRunner {
	public String getSubSystemName();	
}
