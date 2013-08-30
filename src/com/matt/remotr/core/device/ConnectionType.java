package com.matt.remotr.core.device;

import javax.xml.bind.annotation.XmlEnum;

import com.matt.remotr.core.event.EventCoordinator;

/**
 * Used to indicate how the device is connected to the service. 
 * The {@link EventCoordinator} will use this to determine how the event is forwarded to the {@link Device}
 * @author mattm
 *
 */
@XmlEnum
public enum ConnectionType {
	NONE, TCPWS, XMPP
}
