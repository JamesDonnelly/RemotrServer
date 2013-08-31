package com.matt.remotr.ws.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jws.WebMethod;

/**
 * Marker annotation that can be used on methods in a class that implements {@link WsRequestRunner} to indicate that the method can be called via
 * the XMPPWS or TCPWS. <b>Note: This does not also publish the method via the WSDL for SOAP</b>
 * 
 * There is no need to annotate SOAP {@link WebMethod} methods with this annotation also.
 * @author mattm
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsRequestMethod {
	
	/**
	 * Used to override the java method name and present this to the endpoint
	 */
	String operationName() default "";
	
	/**
	 * Used to exclude the method from being published
	 * @return
	 */
	boolean exclude() default false;
}
