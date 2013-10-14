package com.remotr.subsystem.device.resource.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.remotr.subsystem.device.resource.domain.Resource;
import com.remotr.subsystem.event.types.EventType;

@Entity
@Table(name="resource")
public class ResourceJpa implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String resourceName;
	private EventType eventType;
	
	public ResourceJpa() {}
	
	public ResourceJpa(Resource resource){
		this.id = resource.getId();
		this.resourceName = resource.getResourceName();
		this.eventType = resource.getEventType();
	}
	
	@Id
	@GeneratedValue
	@Column(name = "resource_id")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "resource_name")
	public String getResourceName() {
		return resourceName;
	}
	
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	@Column(name = "event_type")
	public EventType getEventType() {
		return eventType;
	}
	
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceJpa other = (ResourceJpa) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
