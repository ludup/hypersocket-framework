package com.hypersocket.plugins;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginServiceImpl {
	static Logger LOG = LoggerFactory.getLogger(PluginServiceImpl.class);
	
	@Autowired
	private ExtensionsPluginManager pluginManager;
	
	@PostConstruct
	private void setup() {
	}
}
