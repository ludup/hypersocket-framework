/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http:www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.servlet.request.Request;
import com.hypersocket.session.json.SessionUtils;

@Component
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

	@Autowired
	private ControllerInterceptor interceptor;

	@Autowired
	private RestApiInterceptor restApiInterceptor;
	
	@Autowired
	private AuthenticationService authenticationService; 

	@Autowired
	private SessionUtils sessionUtils; 
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/**/v1/rest/**");
		registry.addInterceptor(restApiInterceptor).addPathPatterns("/**/v1/rest/**");
		super.addInterceptors(registry);
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		MappingJackson2HttpMessageConverter jc = null;
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				jc = (MappingJackson2HttpMessageConverter) converter;
			}
		}
		converters.remove(jc);
		
		MyJackson2HttpMessageConverter c = new MyJackson2HttpMessageConverter();
		ObjectMapper mapper = jc.getObjectMapper();
		mapper.registerModule(new Hibernate5Module().disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION));
		c.setObjectMapper(mapper);
		
		converters.add(c);
		
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
	
	
	class MyJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

		@Override
		protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {
			if(sessionUtils.hasActiveSession(Request.get())) {
				try(var c = authenticationService.tryAs(sessionUtils.getSession(Request.get()),
						sessionUtils.getLocale(Request.get()))) {
					super.writeInternal(object, type, outputMessage);
					return;
				}
				catch (Exception e) {
				}
			}
			super.writeInternal(object, type, outputMessage);	
		}
		
	}
}
