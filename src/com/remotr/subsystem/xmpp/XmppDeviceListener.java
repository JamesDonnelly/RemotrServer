package com.remotr.subsystem.xmpp;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;

import com.remotr.subsystem.device.DeviceCoordinator;

public class XmppDeviceListener implements ChatManagerListener {
	private Logger log;
	private XmppManager xmppManager;
	private DeviceCoordinator deviceCoordinator;
	
	public XmppDeviceListener(XmppManager xmppCoordinator, DeviceCoordinator deviceCoordinator){
		log = Logger.getLogger(this.getClass());
		this.xmppManager = xmppCoordinator;
		this.deviceCoordinator = deviceCoordinator;
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		if(!createdLocally){
			log.debug("Creating new MessageManager for ["+chat.getParticipant()+"]");
			chat.addMessageListener(new XmppMessageServer(xmppManager, deviceCoordinator));
		}
	}

}
