package com.matt.remotr.core.argument;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
public class Argument implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	private Long commandId;
	private String type;
	private String value;
	private static final long serialVersionUID = 1L;
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@XmlElement(name="ArgumentId")
	public Long getId() {
		return id;
	}
	
	@XmlElement(name="CommandId")
	public Long getCommandId() {
		return commandId;
	}

	public void setCommandId(Long commandId) {
		this.commandId = commandId;
	}

	@XmlElement(name="Type")
	public String getType() {
		return type;
	}
	
	@XmlElement(name="Value")
	public String getValue() {
		return value;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setValue(String value) {
		this.value = value;
	}


}
