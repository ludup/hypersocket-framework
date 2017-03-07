package com.hypersocket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	public <T> T getBean(Class<T> clz) {
		return applicationContext.getBean(clz);
	}
}
