package com.matt.remotr.core.event;

/**
 * Provides a base class on which to build different types of events
 */
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Event {

	private int id;
	private String name;
	private EventType eventType;
	private Object wrappedPayload;
	
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

	@XmlElement(name="EventData")
	public Object getWrappedPayload() {
		return wrappedPayload;
	}

	public void setWrappedPayload(Object wrappedPayload) {
		this.wrappedPayload = wrappedPayload;
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
