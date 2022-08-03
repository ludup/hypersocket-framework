/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;

public interface I18NService {

	boolean isRegistered(String bundle, I18NGroup group);

	void registerBundle(String bundle);
	
	default void registerBundle(String bundle, ClassLoader classLoader) {
		registerBundle(bundle, I18NGroup.DEFAULT_GROUP, classLoader);
	}
	
	void registerBundle(String bundle, I18NGroup group);

	void deregisterBundle(String bundle);
	
	default void deregisterBundle(String bundle, ClassLoader classLoader) {
		deregisterBundle(bundle, I18NGroup.DEFAULT_GROUP, classLoader);
	}
	
	void deregisterBundle(String bundle, I18NGroup group);

	Cache<String,String> getResourceMap(Locale locale);
	
	Cache<String, String> getResourceMap(Locale locale,  I18NGroup group);

	Locale getLocale(String locale);

	List<Locale> getSupportedLocales();

	Map<String, Map<String, Message>> getTranslatableMessages();
	
	Map<String,Map<String,Message>> getTranslatableMessages(I18NGroup group);

	Set<String> getBundles();
	
	Set<String> getBundles(I18NGroup group);

	String getResource(String resourceKey, Locale locale);

	String getResource(String resourceKey);
	
	String getResource(String resourceKey, Locale locale, I18NGroup group);

	String getResource(String resourceKey, I18NGroup group);

	void clearCache(Locale locale);
	
	void clearCache(Locale locale, I18NGroup group);
	
	long getLastUpdate();

	void deregisterBundle(String bundle, I18NGroup group, ClassLoader classLoader);

	void registerBundle(String bundle, I18NGroup group, ClassLoader classLoader);


}
