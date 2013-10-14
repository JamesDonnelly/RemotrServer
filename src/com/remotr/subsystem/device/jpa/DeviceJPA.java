package com.remotr.subsystem.device.jpa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.remotr.device.command.domain.Command;
import com.remotr.subsystem.device.command.jpa.CommandJPA;
import com.remotr.subsystem.device.domain.ConnectionType;
import com.remotr.subsystem.device.domain.Device;
import com.remotr.subsystem.device.domain.DeviceType;
import com.remotr.subsystem.device.resource.domain.Resource;
import com.remotr.subsystem.device.resource.jpa.ResourceJpa;

@NamedQueries(
	@NamedQuery(name="device.getById", query="from DeviceJPA where device_id = :deviceId")
	)
@Entity
@Table(name="device")
public class DeviceJPA implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Long deviceId;
	private String name;
	private DeviceType type;
	private ConnectionType connectionType;
	private Long lastHeatbeatTime;
	private boolean hasHeartbeat = false;
	private Set<CommandJPA> commands;
	private Set<ResourceJpa> resources;
	
	public DeviceJPA() {}
	
	public DeviceJPA(Device device){
		this.deviceId = device.getId();
		this.name = device.getName();
		this.type = device.getType();
		this.connectionType = device.getConnectionType();
		this.lastHeatbeatTime = device.getLastHeartbeatTime();
		this.hasHeartbeat = device.isHadHeartbeat();
		
		if(device.getCommands() != null){
			this.commands = new HashSet<CommandJPA>();
			for(Command c : device.getCommands()){
				commands.add(toJPA(c));
			}
		}
		
		if(device.getResources() != null){
			this.resources = new HashSet<ResourceJpa>();
			for(Resource r : device.getResources()){
				resources.add(toJPA(r));
			}
		}

	}
	
	public void setId(Long id) {
		this.deviceId = id;
	}
	
	@Id
	@GeneratedValue
	@Column(name = "device_id")
	public Long getId() {
		return deviceId;
	}
	
	public void setType(DeviceType type){
		this.type = type;
	}

	@Column(name = "device_type")
	public DeviceType getType() {
		return type;
	}
	
	public void setName(String name){
		this.name = name;
	}

	@Column(name = "device_name")
	public String getName() {
		return name;
	}
	
	public void setLastHeartbeatTime(Long lastHeatbeatTime) {
		this.lastHeatbeatTime = lastHeatbeatTime;
		hasHeartbeat = true;
	}

	@Column(name = "heartbeat_time")
	public Long getLastHeartbeatTime() {
		return lastHeatbeatTime;
	}
	
	public void setHasHeartbeat(boolean hasHeartbeat) {
		this.hasHeartbeat = hasHeartbeat;
	}

	@Column(name = "had_heartbeat")
	public boolean getHasHeartbeat() {
		return hasHeartbeat;
	}
	
	@OneToMany
	@JoinTable(
            name="device_command_link",
            joinColumns = @JoinColumn(name="device_id"),
            inverseJoinColumns = @JoinColumn(name="command_id")
    )
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<CommandJPA> getCommands() {
		return commands;
	}
	
	public void setCommands(Set<CommandJPA> commands) {
		this.commands = commands;
	}

	@Column(name = "connection_type")
	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}
	
	@OneToMany
	@JoinTable(
            name="device_resource_link",
            joinColumns = @JoinColumn(name="device_id"),
            inverseJoinColumns = @JoinColumn(name="resource_id")
    )
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<ResourceJpa> getResources() {
		return resources;
	}
 
	public void setResources(Set<ResourceJpa> resources) {
		this.resources = resources;
	}
	
	private CommandJPA toJPA(Command cmd){
		CommandJPA jpa = new CommandJPA(cmd);
		return jpa;
	}
	
	private ResourceJpa toJPA(Resource r){
		ResourceJpa jpa = new ResourceJpa(r);
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
