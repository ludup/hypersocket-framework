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

import org.springframework.context.event.ContextStartedEvent;

public interface I18NService {

	void registerBundle(String bundle);

	Cache<String, String> getResourceMap(Locale locale);

	Locale getLocale(String locale);

	List<Locale> getSupportedLocales();

	Map<String, Map<String, Message>> getTranslatableMessages();

	Set<String> getBundles();

	String getResource(String resourceKey, Locale locale);

	String getResource(String resourceKey);

	void onContextStarted(ContextStartedEvent event);


}
