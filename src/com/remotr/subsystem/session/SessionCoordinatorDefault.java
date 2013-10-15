package com.remotr.subsystem.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.event.EventCoordinator;
import com.remotr.subsystem.event.EventReceiver;
import com.remotr.subsystem.event.types.Event;
import com.remotr.subsystem.session.domain.DeviceSession;

public class SessionCoordinatorDefault implements SessionCoordinator, EventReceiver {

	private Logger log;
	private EventCoordinator eventCoordinator;
	
	private Map<String, DeviceSession> sessionCache;
	private int sessionTimeout = 120000;
	
	public SessionCoordinatorDefault(){
		log = Logger.getLogger(this.getClass());
		log.info("Starting new SessionCoordinator");
		
		sessionCache = new HashMap<String, DeviceSession>();
	}
	
	@PostConstruct
	public void init(){
		eventCoordinator.registerForEvents(this);
	}
	
	public void setEventCoordinator(EventCoordinator eventCoordinator) {
		this.eventCoordinator = eventCoordinator;
	}
	
	@Override
	public DeviceSession login(Device device) {
		if(device.getSessionKey() == null || !sessionCache.containsKey(device.getSessionKey())){
			// This is a new session
			DeviceSession ds = new DeviceSession();
			ds.setSessionKey(UUID.randomUUID().toString());
			ds.setActive(true);
			updateActiveTime(ds, true);
		}else{
			Iterator<Entry<String, DeviceSession>> it = sessionCache.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = it.next();
		        String s = (String) pairs.getKey();
		        if(s.equals(device.getSessionKey())){
		        	return (DeviceSession) pairs.getValue();
		        }
		    }
		}
		
		return null;
	}

	@Override
	public boolean logout(Device device) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean validate(String sessionKey) {
		if(sessionCache.containsKey(sessionKey)){
			DeviceSession s = sessionCache.get(sessionKey);
			updateActiveTime(s, true);
			return true;
		}else{
			return false;
		}
	}

	@Override
	public int getActionSessionCount() {
		int i = 0;
		Iterator<Entry<String, DeviceSession>> it = sessionCache.entrySet().iterator();
	    while (it.hasNext()) {
	        Entry<String, DeviceSession> pairs = it.next();
	        DeviceSession s = (DeviceSession) pairs.getValue();
	        if(s.isActive()){
	        	i++;
	        }
	    }
		return i;
	}
	
	private void cacheSession(DeviceSession session){
		sessionCache.put(session.getSessionKey(), session);
	}
	
	private DeviceSession updateActiveTime(DeviceSession session, boolean cacheAfter){
		Long now = System.nanoTime();
		session.setLastActive(now);
		
		if(cacheAfter)
			cacheSession(session);
		
		return session;
	}

	@Override
	public void onBroadcastEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
	
}
