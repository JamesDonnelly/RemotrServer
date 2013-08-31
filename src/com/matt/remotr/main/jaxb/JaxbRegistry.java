package com.matt.remotr.main.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class JaxbRegistry {

	 @XmlElementDecl(name="string")
	 public JAXBElement<String> createString(String str) {
		 return new JAXBElement<String>(new QName("string"), String.class, str);
	 }	 
}
