package com.matt.remotr.core.argument.jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.matt.remotr.core.argument.domain.Argument;

@Entity
@Table(name="ArgumentJPA")
public class ArgumentJPA implements Serializable {
	
	@Id
	@GeneratedValue
	private Long id;
	private Long commandId;
	private String type;
	private String value;
	private static final long serialVersionUID = 1L;
	
	public ArgumentJPA(Argument argument){
		this.id = argument.getId();
		this.commandId = argument.getCommandId();
		this.type = argument.getType();
		this.value = argument.getValue();		
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}

	public Long getCommandId() {
		return commandId;
	}

	public void setCommandId(Long commandId) {
		this.commandId = commandId;
	}

	public String getType() {
		return type;
	}

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
