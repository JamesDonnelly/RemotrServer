package com.remotr.subsystem.xmpp;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

public class XmppRosterManager implements RosterListener {
	private Logger log;
	private XmppManager xmppManager;
	
	public XmppRosterManager(XmppManager xmppCoordinator){
		log = Logger.getLogger(this.getClass());
		this.xmppManager = xmppCoordinator;
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		rosterChanged(addresses, "Added");
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		rosterChanged(addresses, "Delete");
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		rosterChanged(addresses, "Updated");
	}

	@Override
	public void presenceChanged(Presence presence) {
		log.debug("XMPP Presence change: From: ["+presence.getFrom()+"] Status: ["+presence.getStatus()+"]");
	}
	
	private void rosterChanged(Collection<String> addresses, String mode){
		for(String address : addresses){
			log.debug("Roster has changed: ["+mode+"] - ["+address+"]");
		}
	}

}
