package com.remotr.subsystem.ws.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marker for each of the parameters that can be exposed on a {@link WsMethod}
 * @author matt
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsParam {
	
	String name();
	
	Class<?> type();

}
