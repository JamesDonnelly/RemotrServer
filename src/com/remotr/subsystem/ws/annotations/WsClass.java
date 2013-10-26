package com.remotr.subsystem.ws.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.remotr.subsystem.ws.response.domain.WsSubsystemHolder;

/**
 * Annotation interface that can be used on classes containing {@link WsMethod}s. 
 * Information set here will set on {@link WsSubsystemHolder}
 * @author matt
 *
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsClass {
	
	/**
	 * Description of the service class
	 */
	String description();

}
