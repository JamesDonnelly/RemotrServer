package com.matt.remotr.core.event;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Event {

	private int id;
	private String name;
	private EventType eventType;
	
	@XmlElement(name="ID")
	public int getId() {
		return id;
	}
	
	@XmlElement(name="Name")
	public String getName() {
		return name;
	}
	
	@XmlElement(name="Type")
	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setName(String eventName) {
		this.name = eventName;
	}

}
