/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http:www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

@Component
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {


	@Autowired
	ControllerInterceptor interceptor;

	@Autowired
	RestApiInterceptor restApiInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/**/v1/rest/**");
		registry.addInterceptor(restApiInterceptor).addPathPatterns("/**/v1/rest/**");
		super.addInterceptors(registry);
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				ObjectMapper mapper = ((MappingJackson2HttpMessageConverter) converter).getObjectMapper();
				mapper.registerModule(new Hibernate5Module().disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION));
			}
		}
		super.extendMessageConverters(converters);
	}
	
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
		super.configureContentNegotiation(configurer);
	}
	
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}
	
}
