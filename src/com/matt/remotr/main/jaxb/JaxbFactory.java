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
					com.matt.remotr.core.argument.Argument.class,
					com.matt.remotr.core.command.Command.class,
					com.matt.remotr.core.device.DeviceType.class,
					com.matt.remotr.core.device.ConnectionType.class,
					com.matt.remotr.core.device.Device.class,
					
					// Event classes
					com.matt.remotr.core.event.EventType.class,
					com.matt.remotr.core.event.types.Event.class,
					com.matt.remotr.core.event.types.JobEvent.class,
					com.matt.remotr.core.event.types.DeviceEvent.class,
					
					// Response classes
					com.matt.remotr.ws.response.WsResponse.class,
					com.matt.remotr.ws.response.WsDeviceResponse.class,
					com.matt.remotr.ws.response.WsJobResponse.class,
					
					// Request classes
					com.matt.remotr.ws.request.WsRequest.class,
					com.matt.remotr.ws.request.WsRequestParameter.class,
					
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
