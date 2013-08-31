package com.matt.remotr.ws.response;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.matt.remotr.main.Main;
import com.matt.remotr.ws.request.WsRequest;

/**
 * Lets any Jaxb annotated object be wrapped and exported via the web service methods.
 * For more specific wrapping, this class should be extended (Example: {@link WsDeviceResponse})
 * @author mattm
 *
 */

@XmlRootElement
public class WsResponse {
	
	// Fields that we may want to add regarding the WsCall
	/**
	 * If not set explicitly, success defaults to false 
	 */
	private boolean success = false;
	private String subSystem;
	
	/**
	 * This can be set to any string on the {@link WsRequest} and will be returned on the response
	 */
	private String reference;
	/**
	 * If not set explicitly, dateTime defaults to the time at which the response object was created
	 */
	private Long dateTime = System.currentTimeMillis();
	
	// The actual response objects (in this case, generic)
	private Object response;
	private ArrayList<?> listResponse;	
	
	// Wrap up an error message, this is not an exception
	private String errorMessage;
	
	// Wrapping of Exceptions
	private String exceptionMessage;	
	private String exceptionCause;
	
	//Version info
	private String versionNum = Main.getVersionNumber();
	private String versionName = Main.getVersionName();
	
	
	// Information about the WsCall
	@XmlAttribute(name="Success")
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@XmlAttribute(name="SubSystem")
	public String getSubSystem() {
		return subSystem;
	}

	public void setSubSystem(String subSystem) {
		this.subSystem = subSystem;
	}
	
	@XmlAttribute(name="DateTime")
	public String getDateTime() {
		// As this is only for display, show it to the user nicely. If it is needed as a Long, then the extended class should implement
		return convertTime(dateTime);
	}

	public void setDateTime(Long dateTime) {
		this.dateTime = dateTime;
	}
	
	// The wrapped objects
	@XmlElement(name="WrappedObject")
	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	@XmlElement(name="WrappedObject")
	@XmlElementWrapper(name="WrappedObjects")
	public ArrayList<?> getListResponse() {
		return listResponse;
	}

	public void setListResponse(ArrayList<?> listResponse) {
		this.listResponse = listResponse;
	}
	
	// Deal with any errors that need to be reported back
	@XmlElement(name="Exception")
	public String getException() {
		return exceptionMessage;
	}
	
	@XmlElement(name="ExceptionStack")
	public String getExceptionCause() {
		return exceptionCause;
	}

	public void setException(Exception exception) {
		if(exception.getMessage().equals("") || exception.getMessage() == null){
			this.exceptionMessage = "No Message Given";
		}else{
			this.exceptionMessage = exception.getMessage();
		}
		StringWriter stack = new StringWriter();
		exception.printStackTrace((new PrintWriter(stack)));
		this.exceptionCause = stack.toString();
	}

	@XmlElement(name="Error")
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@XmlAttribute(name="VersionNumber")
	public String getVersionNum() {
		return versionNum;
	}

	@XmlAttribute(name="VersionName")
	public String getVersionName() {
		return versionName;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@XmlAttribute(name="Reference")
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	// Helper functions
	private String convertTime(long time){
	    Date date = new Date(time);
	    Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    return format.format(date).toString();
	}

}
