package com.matt.remotr.ws.request;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * Used for sending parameters with {@link WsRequest} messages.
 * @author mattm
 *
 */
@XmlRootElement
public class WsRequestParameter {
	private String classType;
	private Object value;
	
	@XmlElement(name="ClassType")
	public String getClassType() {
		return classType;
	}
	
	@XmlAnyElement(lax=true)
	public Object getValue() {
		return value;
	}
	
	public void setClassType(String classType) {
		this.classType = classType;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

}
