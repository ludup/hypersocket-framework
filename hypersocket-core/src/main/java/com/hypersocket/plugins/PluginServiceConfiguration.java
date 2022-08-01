package com.hypersocket.plugins;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginServiceConfiguration {

	@Bean
	public ExtensionsPluginManager pluginManager() {
		return new ExtensionsPluginManager();
	}

//	@Bean
//	@DependsOn("pluginManager")
//	public AppExtensions appExtensions() {
//		return new AppExtensions();
//	}
}
