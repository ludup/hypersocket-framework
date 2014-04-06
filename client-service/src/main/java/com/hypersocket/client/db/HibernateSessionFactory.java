package com.hypersocket.client.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import com.hypersocket.client.Main;

public class HibernateSessionFactory {

	static SessionFactory factory;
	

	public static SessionFactory getFactory() {
		if (factory == null) {
			synchronized (Main.class) {
				if (factory == null) {
					factory = new AnnotationConfiguration().configure()
							.buildSessionFactory();
				}
			}
		}
		return factory;
	}
	
}
