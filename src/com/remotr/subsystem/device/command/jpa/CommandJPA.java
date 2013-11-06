package com.remotr.subsystem.device.command.jpa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.remotr.subsystem.device.argument.domain.Argument;
import com.remotr.subsystem.device.argument.jpa.ArgumentJPA;
import com.remotr.subsystem.device.command.domain.Command;

@Entity
@Table(name="command")
public class CommandJPA implements Serializable {
	
	private Long commandId;
	private String name;
	private String commandTrigger;
	private Set<ArgumentJPA> arguments;
	private static final long serialVersionUID = 1L;
	
	public CommandJPA() {}
	
	public CommandJPA(Command command){
		this.commandId = command.getId();
		this.name = command.getName();
		this.commandTrigger = command.getTrigger();
		
		if(command.getArguments() != null){
			this.arguments = new HashSet<ArgumentJPA>();
			for(Argument a : command.getArguments()){
				arguments.add(toJPA(a));
			}
		}
	}
	
	public void setId(Long id) {
		this.commandId = id;
	}

	@Id
	@GeneratedValue
	@Column(name = "command_id")
	public Long getId() {
		return commandId;
	}

	@Column(name = "command_name")
	public String getName() {
		return name;
	}
	
	@Column(name = "trigger_name")
	public String getTrigger() {
		return commandTrigger;
	}
	
	@OneToMany
	@JoinTable(
            name="command_argument_link",
            joinColumns = @JoinColumn(name="command_id"),
            inverseJoinColumns = @JoinColumn(name="argument_id")
    )
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<ArgumentJPA> getArguments() {
		return arguments;
	}
	

	public void setArguments(Set<ArgumentJPA> arguments) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((commandId == null) ? 0 : commandId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		CommandJPA other = (CommandJPA) obj;
		if (commandId == null) {
			if (other.commandId != null)
				return false;
		} else if (!commandId.equals(other.commandId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
