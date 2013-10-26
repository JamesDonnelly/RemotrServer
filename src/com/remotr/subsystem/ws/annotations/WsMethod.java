package com.remotr.subsystem.ws.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.remotr.subsystem.ws.WsRunner;

/**
 * Marker annotation that can be used on methods in a class that implements {@link WsRunner} to indicate that the method can be called via
 * the XMPPWS, TCPWS or the RESTful service.
 * 
 * @author mattm
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsMethod {
	
	
	/**
	 * Used to override the java method name and present this to the endpoint
	 */
	String operationName() default "";
	
	/**
	 * Description given to the WebService method
	 */
	String description() default "No description given";
	
	/**
	 * List of all parameters that are exposed to the WebService.
	 */
	WsParam[] wsParams() default {};
	
	/**
	 * Used to exclude the method from being published
	 */
	boolean exclude() default false;
	
	/**
	 * Sets is this is a public WsMethod. Public WsMethods do not require a valid {@link DeviceSession}
	 */
	boolean isPublic() default false;
	
	/**
	 * Set this to true if the method is asynchronous. This stops it being accessed via a RESTful call 
	 * @return
	 */
	boolean isAsync() default false;
}
