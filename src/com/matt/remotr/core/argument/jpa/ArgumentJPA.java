package com.matt.remotr.core.argument.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.matt.remotr.core.argument.domain.Argument;

@Entity
@Table(name="argument")
public class ArgumentJPA implements Serializable {
	
	private Long id;
	private String type;
	private String value;
	private static final long serialVersionUID = 1L;
	
	public ArgumentJPA(){}
	
	public ArgumentJPA(Argument argument){
		this.id = argument.getId();
		this.type = argument.getType();
		this.value = argument.getValue();		
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Id
	@GeneratedValue
	@Column(name = "argument_id")
	public Long getId() {
		return id;
	}

	@Column(name = "argument_type")
	public String getType() {
		return type;
	}

	@Column(name = "argument_value")
	public String getValue() {
		return value;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setValue(String value) {
		this.value = value;
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
		ArgumentJPA other = (ArgumentJPA) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


}
