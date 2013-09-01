package com.matt.remotr.core.device.jpa;

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

import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.command.jpa.CommandJPA;
import com.matt.remotr.core.device.domain.ConnectionType;
import com.matt.remotr.core.device.domain.Device;
import com.matt.remotr.core.device.domain.DeviceType;

@Entity
@Table(name="DeviceJPA")
public class DeviceJPA implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private Long deviceId;
	private String name;
	private DeviceType type;
	private ConnectionType connectionType;
	private Long lastHeatbeatTime;
	private boolean hasHeartbeat = false;
	private ArrayList<CommandJPA> commands;
	
	public DeviceJPA() {}
	
	public DeviceJPA(Device device){
		this.deviceId = device.getId();
		this.name = device.getName();
		this.type = device.getType();
		this.connectionType = device.getConnectionType();
		this.lastHeatbeatTime = device.getLastHeartbeatTime();
		this.hasHeartbeat = device.isHadHeartbeat();
		
		if(device.getCommands() != null){
			this.commands = new ArrayList<CommandJPA>();
			for(Command c : device.getCommands()){
				commands.add(toJPA(c));
			}
		}

	}
	
	public void setId(Long id) {
		this.deviceId = id;
	}
	
	public Long getId() {
		return deviceId;
	}
	
	public void setType(DeviceType type){
		this.type = type;
	}

	public DeviceType getType() {
		return type;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setLastHeatbeatTime(Long lastHeatbeatTime) {
		this.lastHeatbeatTime = lastHeatbeatTime;
		hasHeartbeat = true;
	}

	public Long getLastHeartbeatTime() {
		return lastHeatbeatTime;
	}
	
	public void setHasHeartbeat(boolean hasHeartbeat) {
		this.hasHeartbeat = hasHeartbeat;
	}

	public boolean isHadHeartbeat() {
		return hasHeartbeat;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="deviceId")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public ArrayList<CommandJPA> getCommands() {
		return commands;
	}
	
	public void setCommands(ArrayList<CommandJPA> commands) {
		this.commands = commands;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}
	
	private CommandJPA toJPA(Command cmd){
		CommandJPA jpa = new CommandJPA(cmd);
		return jpa;
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
		DeviceJPA other = (DeviceJPA) obj;
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
