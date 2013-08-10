package com.matt.remotr.core.command;

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

import com.matt.remotr.core.argument.Argument;

@XmlRootElement
@Entity
public class Command implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long commandId;
	
	private Long deviceId;
	private String name;
	private String commandTrigger;
	private ArrayList<Argument> arguments;
	private static final long serialVersionUID = 1L;
	
	public void setId(Long id) {
		this.commandId = id;
	}
	
	@XmlElement(name="CommandId")
	public Long getId() {
		return commandId;
	}
	
	@XmlElement(name="DeviceId")
	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	@XmlElement(name="Name")
	public String getName() {
		return name;
	}
	
	@XmlElement(name="Trigger")
	public String getTrigger() {
		return commandTrigger;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="commandId")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	@XmlElement(name="Argument")
	@XmlElementWrapper(name="Arguments")
	public ArrayList<Argument> getArguments() {
		return arguments;
	}
	

	public void setArguments(ArrayList<Argument> arguments) {
		this.arguments = arguments;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTrigger(String trigger) {
		this.commandTrigger = trigger;
	}

	
}
