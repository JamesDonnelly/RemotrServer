package com.matt.remotr.core.event;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum EventType {
	// Broadcast
	BROADCAST,
	
	// Job Types
	JOB,
	
	// Ping Types
	PING,
	
	// Message Types
	MESSAGE,
	
	// XMPP Types
	XMPP,
	
	// Device types
	DEVICE_REGISTER, 
	DEVICE_UNREGISTER
}
