package com.matt.remotr.ws.request;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.matt.remotr.ws.response.WsResponse;

@XmlRootElement
public class WsRequest {
	
	private Long dateTime = System.currentTimeMillis();
	private String subSystem;
	private String method;
	private ArrayList<WsRequestParameter> params;
	
	/**
	 * Can be set to any string and will be returned on the {@link WsResponse}
	 */
	private String reference;
	
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
	
	@XmlAttribute(name="Reference")
	public String getReference() {
		return reference;
	}

	@XmlElement(name="Param")
	@XmlElementWrapper(name="Params")
	public ArrayList<WsRequestParameter> getParams() {
		return params;
	}

	public void setParams(ArrayList<WsRequestParameter> params) {
		this.params = params;
	}

	public void setReference(String reference) {
		this.reference = reference;
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
