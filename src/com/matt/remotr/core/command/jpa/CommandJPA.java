package com.matt.remotr.core.command.jpa;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.matt.remotr.core.argument.domain.Argument;
import com.matt.remotr.core.argument.jpa.ArgumentJPA;
import com.matt.remotr.core.command.domain.Command;

@Entity
@Table(name="CommandJPA")
public class CommandJPA implements Serializable {
	
	@Id
	@GeneratedValue
	private Long commandId;
	private Long deviceId;
	private String name;
	private String commandTrigger;
	private ArrayList<ArgumentJPA> arguments;
	private static final long serialVersionUID = 1L;
	
	public CommandJPA(Command command){
		this.commandId = command.getId();
		this.deviceId = command.getDeviceId();
		this.name = command.getName();
		this.commandTrigger = command.getTrigger();
		
		if(command.getArguments() != null){
			this.arguments = new ArrayList<ArgumentJPA>();
			for(Argument a : command.getArguments()){
				arguments.add(toJPA(a));
			}
		}
	}
	
	public void setId(Long id) {
		this.commandId = id;
	}

	public Long getId() {
		return commandId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}
	
	public String getTrigger() {
		return commandTrigger;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="commandId")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public ArrayList<ArgumentJPA> getArguments() {
		return arguments;
	}
	

	public void setArguments(ArrayList<ArgumentJPA> arguments) {
		this.arguments = arguments;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTrigger(String trigger) {
		this.commandTrigger = trigger;
	}
	
	private ArgumentJPA toJPA(Argument arg){
		ArgumentJPA jpa = new ArgumentJPA(arg);
		return jpa;
	}

	
}
