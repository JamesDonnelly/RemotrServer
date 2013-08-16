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
					// Device classes
					com.matt.remotr.core.argument.Argument.class,
					com.matt.remotr.core.command.Command.class,
					com.matt.remotr.core.device.DeviceType.class,
					com.matt.remotr.core.device.Device.class,
					
					// Event classes
					com.matt.remotr.core.event.EventType.class,
					com.matt.remotr.core.event.Event.class,
					com.matt.remotr.core.event.JobEvent.class,
					
					// Response classes
					com.matt.remotr.ws.response.WsResponse.class,
					com.matt.remotr.ws.response.WsDeviceResponse.class,
					com.matt.remotr.ws.response.WsJobResponse.class,
					
					// Job classes
					com.matt.remotr.core.job.JobStatus.class
					});
			
			marshaller = context.createMarshaller();
			unmarshaller = context.createUnmarshaller();
			
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		}catch(JAXBException e) {
	       //
	    }
	}
	
	public static Marshaller getMarshaller(){
		return marshaller;
	}
	
	public static Unmarshaller getUnmarshaller(){
		return unmarshaller;
	}
}
