package com.matt.remotr.core.device;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.matt.remotr.core.command.Command;

@XmlRootElement
@Entity
public class Device implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long deviceId;
	private String name;
	private DeviceType type;
	private Long lastHeatbeatTime;
	private boolean hasHeartbeat = false;
	private ArrayList<Command> commands;
	
	public void setId(Long id) {
		this.deviceId = id;
	}
	
	@XmlElement(name="DeviceId")
	public Long getId() {
		return deviceId;
	}
	
	public void setType(DeviceType type){
		this.type = type;
	}
	
	@XmlElement(name="Type")
	public DeviceType getType() {
		return type;
	}
	
	public void setName(String name){
		this.name = name;
	}

	@XmlElement(name="Name")
	public String getName() {
		return name;
	}
	
	public void setLastHeatbeatTime(Long lastHeatbeatTime) {
		this.lastHeatbeatTime = lastHeatbeatTime;
		hasHeartbeat = true;
	}

	@XmlElement(name="LastHeartbeatTime")
	public Long getLastHeartbeatTime() {
		return lastHeatbeatTime;
	}
	
	public void setHasHeartbeat(boolean hasHeartbeat) {
		this.hasHeartbeat = hasHeartbeat;
	}

	@XmlElement(name="HasReceivedHeartbeat")
	public boolean isHadHeartbeat() {
		return hasHeartbeat;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="deviceId")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	@XmlElement(name="Command")
	@XmlElementWrapper(name="Commands")
	public ArrayList<Command> getCommands() {
		return commands;
	}
	
	public void setCommands(ArrayList<Command> commands) {
		this.commands = commands;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Device other = (Device) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}



}
