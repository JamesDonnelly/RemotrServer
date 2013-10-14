package com.remotr.subsystem.ws;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import com.remotr.core.jaxb.JaxbFactory;

/**
 * Use the JAXB context from the factory in core.jaxb
 * @author matt
 *
 */
@Provider
@Produces({"text/xml"})
public class JaxbContextResolver implements ContextResolver<JAXBContext>{
	
	private JAXBContext context;

	public JaxbContextResolver() throws Exception {
		this.context = JAXBContext.newInstance(JaxbFactory.getTypes());
	}

	public JAXBContext getContext(Class<?> objectType) {
		for (Class<?> type : JaxbFactory.getTypes()) {
			if (type == objectType) {
				return context;
			}
		}
		return null;
	}

}
