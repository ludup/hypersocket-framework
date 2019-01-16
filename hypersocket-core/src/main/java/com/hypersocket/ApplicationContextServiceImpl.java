package com.hypersocket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplicationContextServiceImpl {

	@Autowired
	ApplicationContext applicationContext;
	
	static ApplicationContextServiceImpl instance;
	
	@PostConstruct
	private void postConstruct() {
		instance = this;
	}
	
	public static ApplicationContextServiceImpl getInstance() {
		return instance;
	}
	
	public static boolean isReady() {
		return instance!=null;
	}
	
	public <T> T getBean(Class<T> clz) {
		return applicationContext.getBean(clz);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getBean(String name, Class<T> clz) {
		return (T) applicationContext.getBean(name);
	}
	
	public boolean containsBean(String name) {
		return applicationContext.containsBean(name);
	}

	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
		return applicationContext.getAutowireCapableBeanFactory();
	}
}
