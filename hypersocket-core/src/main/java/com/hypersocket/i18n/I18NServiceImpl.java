/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.certs.CertificateService;
import com.hypersocket.config.ConfigurationChangedEvent;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.SessionService;

@Service
public class I18NServiceImpl extends AuthenticatedServiceImpl implements I18NService, ApplicationListener<ConfigurationChangedEvent> {

	static Logger log = LoggerFactory.getLogger(I18NServiceImpl.class);
	
	Set<String> bundles = new HashSet<String>();
	
	static final String RESOURCE_BUNDLE = "I18NService";
	
	@Autowired
	ConfigurationService configurationService;
	
	Locale defaultLocale;
	
	List<Locale> supportedLocales = new ArrayList<Locale>();
	
	List<Message> allMessages;
	
	@PostConstruct
	private void postConstruct() {
		
		registerBundle(RESOURCE_BUNDLE);
		registerBundle(ConfigurationService.RESOURCE_BUNDLE);
		registerBundle(AuthenticationService.RESOURCE_BUNDLE);
		registerBundle(CertificateService.RESOURCE_BUNDLE);
		registerBundle(EmailNotificationService.RESOURCE_BUNDLE);
		registerBundle(LocalRealmProvider.RESOURCE_BUNDLE);
		registerBundle(PermissionService.RESOURCE_BUNDLE);
		registerBundle(RealmService.RESOURCE_BUNDLE);
		registerBundle(SessionService.RESOURCE_BUNDLE);
		
		supportedLocales.add(Locale.ENGLISH);
//		supportedLocales.add(getLocale("da"));
//		supportedLocales.add(getLocale("nl"));
//		supportedLocales.add(getLocale("fi"));
//		supportedLocales.add(getLocale("fr"));
//		supportedLocales.add(getLocale("de"));
//		supportedLocales.add(getLocale("it"));
//		supportedLocales.add(getLocale("ja"));
//		supportedLocales.add(getLocale("no"));
//		supportedLocales.add(getLocale("pl"));
//		supportedLocales.add(getLocale("ru"));
//		supportedLocales.add(getLocale("es"));
//		supportedLocales.add(getLocale("sv"));
		
	}
	
	@Override
	public void registerBundle(String bundle) {
		bundles.add(bundle);
	}

	@Override
	public Map<String,String> getResourceJson(Locale locale) {
		HashMap<String,String> resources = new HashMap<String,String>();
		for(String bundle : bundles) {
			buildBundleJson(bundle, locale, resources);
		}
		return resources;
	}
	
	private void buildBundleJson(String bundle, Locale locale, Map<String,String> resources) {

		for(String key :  I18N.getResourceKeys(locale, bundle)) {
			resources.put(key, I18N.getResource(locale, bundle, key));
		}
	}

	@Override
	public synchronized Locale getDefaultLocale() {
		if(defaultLocale==null) {
			String locale = configurationService.getValue("current.locale");
			return defaultLocale = getLocale(locale);
		} else {
			return defaultLocale;
		}
	}
	
	@Override
	public Locale getLocale(String locale) {
		for(Locale l : Locale.getAvailableLocales()) {
			if(l.toString().equals(locale)) {
				return l;
			}
		}
		if(log.isWarnEnabled()) {
			log.warn(locale + " is missing");
		}
		return Locale.ENGLISH;
	}
	
	public static String tagForConversion(String resourceBundle, String resourceKey) {
		return "i18n/" + resourceBundle + "/" + resourceKey;
	}
	
	public static String convertFromTag(String tag, Locale locale, Object... arguments) {
		if(tag.startsWith("i18n/")) {
			String[] elements = tag.split("/");
			return I18N.getResource(locale, elements[1], elements[2], arguments);
		} else {
			return tag;
		}
	}
	

	
	@Override
	public Map<String,Map<String,Message>> getTranslatableMessages() {
		
		Map<String,Map<String,Message>> messages = new HashMap<String,Map<String,Message>>();
		
		for(String bundle : bundles) {
			messages.put(bundle, new HashMap<String,Message>());
			for(String key :I18N.getResourceKeys(Locale.ENGLISH, bundle)) {
				String translated = I18N.getResource(Locale.ENGLISH, bundle, key);
				String original = I18N.getResourceNoOveride(Locale.ENGLISH, bundle, key);
				messages.get(bundle).put(key, new Message(bundle, key, original, original.equals(translated) ? "" : translated));
			}
		}
		
		return messages;
	}
	
	@Override
	public boolean hasUserLocales() {
		return configurationService.getBooleanValue("user.locales");
	}

	@Override
	public void onApplicationEvent(ConfigurationChangedEvent event) {
		if(event.getResourceKey().equals(ConfigurationChangedEvent.EVENT_RESOURCE_KEY)) {
			String resourceKey = (String)event.getAttribute(ConfigurationChangedEvent.ATTR_CONFIG_RESOURCE_KEY);
			if(resourceKey.equals("current.locale")) {
				defaultLocale = null;
				defaultLocale = getDefaultLocale();
			}
		}
	}

	@Override
	public List<Locale> getSupportedLocales() {
		return supportedLocales;
	}

	@Override
	public Set<String> getBundles() {
		return bundles;
	}
	
}
