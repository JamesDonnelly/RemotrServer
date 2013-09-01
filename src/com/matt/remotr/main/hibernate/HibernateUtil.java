package com.matt.remotr.main.hibernate;

import java.util.ArrayList;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.matt.remotr.core.argument.domain.Argument;
import com.matt.remotr.core.command.domain.Command;
import com.matt.remotr.core.device.domain.Device;

@SuppressWarnings("rawtypes")
public class HibernateUtil {
	private static final SessionFactory sessionFactory;
	private static final ServiceRegistry serviceRegistry;
	private static final ArrayList<Class> annotatedClassList = new ArrayList<Class>();
	
	static {
		try {
			annotatedClassList.add(Device.class);
			annotatedClassList.add(Command.class);
			annotatedClassList.add(Argument.class);
			
			Configuration configuration = new Configuration();
			configuration.configure();
			for(Class c : annotatedClassList){
				configuration.addAnnotatedClass(c);
			}
			
			serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();        
		    sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
