package com.hypersocket.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationContextProvider implements ApplicationContextAware{
	
	private static ApplicationContext applicationContext;
	
	public static ApplicationContext getApplicationContext(){
		if(applicationContext == null){
			throw new IllegalStateException("applicationContext is null, probably you are accessing it before Spring is up.");
		}
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringApplicationContextProvider.applicationContext = applicationContext;
	}

}
