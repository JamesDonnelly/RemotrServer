package com.matt.remotr.ws.request;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.matt.remotr.ws.response.WsResponse;

@XmlRootElement
public class WsRequest {
	
	private Long dateTime = System.currentTimeMillis();
	private String subSystem;
	private String method;
	
	/**
	 * Can be set to any string and will be returned on the {@link WsResponse}
	 */
	private String refference;
	
	@XmlElement(name="SubSystem")
	public String getSubSystem() {
		return subSystem;
	}
	
	@XmlElement(name="Method")
	public String getMethod() {
		return method;
	}
	
	@XmlAttribute(name="DateTime")
	public Long getDateTime() {
		return dateTime;
	}
	
	@XmlAttribute(name="Refference")
	public String getRefference() {
		return refference;
	}

	public void setRefference(String refference) {
		this.refference = refference;
	}

	public void setSubSystem(String subSystem) {
		this.subSystem = subSystem;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public void setDateTime(Long dateTime) {
		this.dateTime = dateTime;
	}

}
