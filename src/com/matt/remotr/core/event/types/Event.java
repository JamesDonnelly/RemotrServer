package com.matt.remotr.core.event.types;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.matt.remotr.core.event.EventType;

@XmlRootElement
public class Event {

	private int id;
	private String name;
	private EventType eventType;
	private String refference;
	
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

	@XmlAttribute(name="Refference")
	public String getRefference() {
		return refference;
	}

	public void setRefference(String refference) {
		this.refference = refference;
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
