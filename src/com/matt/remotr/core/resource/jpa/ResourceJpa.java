package com.matt.remotr.core.resource.jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.matt.remotr.core.event.types.EventType;
import com.matt.remotr.core.resource.domain.Resource;

@Entity
@Table(name="ResourceJPA")
public class ResourceJpa implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private Long id;
	private Long deviceId;
	private String resourceName;
	private EventType eventType;
	
	public ResourceJpa() {}
	
	public ResourceJpa(Resource resource){
		this.id = resource.getId();
		this.deviceId = resource.getDeviceId();
		this.resourceName = resource.getResourceName();
		this.eventType = resource.getEventType();
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public EventType getEventType() {
		return eventType;
	}
	
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
}
