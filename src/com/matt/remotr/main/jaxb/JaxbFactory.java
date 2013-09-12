package com.matt.remotr.main.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbFactory {
	private static Marshaller marshaller = null;
	private static Unmarshaller unmarshaller = null;
	
	static{
		try{
			JAXBContext context = JAXBContext.newInstance(new Class[] {
					// Jaxb Registry
					com.matt.remotr.main.jaxb.JaxbRegistry.class,
					
					// Device classes
					com.matt.remotr.core.argument.domain.Argument.class,
					com.matt.remotr.core.command.domain.Command.class,
					com.matt.remotr.core.device.domain.DeviceType.class,
					com.matt.remotr.core.device.domain.ConnectionType.class,
					com.matt.remotr.core.device.domain.Device.class,
					
					//Resource classes
					com.matt.remotr.core.resource.domain.Resource.class,
					
					// Event classes
					com.matt.remotr.core.event.types.EventType.class,
					com.matt.remotr.core.event.types.Event.class,
					com.matt.remotr.core.job.JobEvent.class,
					com.matt.remotr.core.event.types.DeviceEvent.class,
					
					// Response classes
					com.matt.remotr.ws.response.domain.WsResponse.class,
					com.matt.remotr.ws.response.domain.WsDeviceResponse.class,
					com.matt.remotr.ws.response.WsJobResponse.class,
					
					// Request classes
					com.matt.remotr.ws.request.domain.WsRequest.class,
					com.matt.remotr.ws.request.domain.WsRequestParameter.class,
					
					// Job classes
					com.matt.remotr.core.job.JobStatus.class
					});
			
			marshaller = context.createMarshaller();
			unmarshaller = context.createUnmarshaller();
			
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		}catch(JAXBException e) {
	       System.out.println(e);
	    }
	}
	
	public static Marshaller getMarshaller(){
		return marshaller;
	}
	
	public static Unmarshaller getUnmarshaller(){
		return unmarshaller;
	}
}
