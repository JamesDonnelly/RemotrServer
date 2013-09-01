package com.matt.remotr.main.hibernate;

import java.util.ArrayList;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.matt.remotr.core.argument.jpa.ArgumentJPA;
import com.matt.remotr.core.command.jpa.CommandJPA;
import com.matt.remotr.core.device.jpa.DeviceJPA;

@SuppressWarnings("rawtypes")
public class HibernateUtil {
	private static final SessionFactory sessionFactory;
	private static final ServiceRegistry serviceRegistry;
	private static final ArrayList<Class> annotatedClassList = new ArrayList<Class>();
	
	static {
		try {
			annotatedClassList.add(DeviceJPA.class);
			annotatedClassList.add(CommandJPA.class);
			annotatedClassList.add(ArgumentJPA.class);
			
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
