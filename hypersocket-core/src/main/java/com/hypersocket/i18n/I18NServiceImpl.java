/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import javax.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.cache.CacheService;
import com.hypersocket.certificates.CertificateResourceService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.secret.SecretKeyServiceImpl;
import com.hypersocket.session.SessionService;

@Service
public class I18NServiceImpl implements I18NService {

	public static final String RESOURCE_BUNDLE = "I18NService";
	public static final String USER_INTERFACE_BUNDLE = "UserInterface";
	
	static Logger log = LoggerFactory.getLogger(I18NServiceImpl.class);
	
	@Autowired
	private CacheService cacheService;
	
	private Set<String> bundles = new HashSet<String>();
	private long lastUpdate = System.currentTimeMillis(); 
	private HashMap<Locale,Map<String,String>> resources = new HashMap<Locale,Map<String,String>>();
	private List<Locale> supportedLocales = new ArrayList<Locale>();
	
	public static String convertFromTag(String tag, Locale locale, Object... arguments) {
		if(tag.startsWith("i18n/")) {
			String[] elements = tag.split("/");
			return I18N.getResource(locale, elements[1], elements[2], arguments);
		} else {
			return tag;
		}
	}
	
	public static String tagForConversion(String resourceBundle, String resourceKey) {
		return "i18n/" + resourceBundle + "/" + resourceKey;
	}
	
	@Override
	public void clearCache(Locale locale) {
		lastUpdate = System.currentTimeMillis();
		cacheService.getCacheManager().destroyCache(getCacheKey(locale));
	}
	
	@Override
	public Set<String> getBundles() {
		return bundles;
	}

	@Override
	public long getLastUpdate() {
		return lastUpdate;
	}
	
	@Override
	public Locale getLocale(String locale) {
		
		Locale result = Locale.forLanguageTag(locale);
		if(result==null) {
			return Locale.ENGLISH;
		}
		return result;
	}
	
	@Override
	public String getResource(String resourceKey) {
		return getResource(resourceKey, Locale.getDefault());
	}
	
	@Override
	public String getResource(String resourceKey, Locale locale) {
		return getResourceMap(locale).get(resourceKey);
	}

	@Override
	public synchronized Cache<String,String> getResourceMap(Locale locale) {
		
		String cacheKey = getCacheKey(locale);
		Cache<String,String> cache = cacheService.getCacheIfExists(cacheKey,String.class, String.class);

		if(cache==null) {
			cache = cacheService.getCacheOrCreate(cacheKey, String.class, String.class);
			buildCache(cache, locale);
		}
		return cache;
	}
	
	@Override
	public List<Locale> getSupportedLocales() {
		return supportedLocales;
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
	public synchronized void registerBundle(String bundle) {
		if(bundles.contains(bundle)) {
			/* TODO: These should actually be fatal so they can be caught earlier */
			log.warn(String.format("Attempt to register bundle that has already been registered with name %s", bundle));
		}
		else {
			/* TODO: These should actually be fatal so they can be caught earlier */
			if(!I18N.bundleExists(bundle))
				log.warn(String.format("Attempt to register bundle with name %s that does not exist anywhere on the classpath (in 'i18n' resource folder).", bundle));
			else {
				bundles.add(bundle);
				resources.clear();
			}
		}
	}

	private void buildBundleMap(String bundle, Locale locale, Cache<String,String> resources) {
		for(String key :  I18N.getResourceKeys(locale, bundle)) {
			resources.put(key, I18N.getResource(locale, bundle, key));
		}
	}

	private void buildCache(Cache<String,String> cache, Locale locale) {
		if(log.isInfoEnabled()) {
			log.info(String.format("Building i18n resources cache for %s", locale.getLanguage()));
		}
		for(String bundle : bundles) {
			buildBundleMap(bundle, locale, cache);
		}
		if(log.isInfoEnabled()) {
			log.info(String.format("Completed i18n resources cache for %s", locale.getLanguage()));
		}
	}


	private String getCacheKey(Locale locale) {
		return String.format("i18n-%s", locale.getLanguage());
	}

	@PostConstruct
	private void postConstruct() {
		
		registerBundle(RESOURCE_BUNDLE);
		registerBundle(ConfigurationService.RESOURCE_BUNDLE);
		registerBundle(AuthenticationService.RESOURCE_BUNDLE);
		registerBundle(CertificateResourceService.RESOURCE_BUNDLE);
		registerBundle(EmailNotificationService.RESOURCE_BUNDLE);
		registerBundle(PermissionService.RESOURCE_BUNDLE);
		registerBundle(RealmService.RESOURCE_BUNDLE);
		registerBundle(SessionService.RESOURCE_BUNDLE);
		registerBundle(SecretKeyServiceImpl.RESOURCE_BUNDLE);
		
		registerBundle(USER_INTERFACE_BUNDLE);
		
		supportedLocales.add(Locale.ENGLISH);
		supportedLocales.add(getLocale("da"));
		supportedLocales.add(getLocale("nl"));
		supportedLocales.add(getLocale("fi"));
		supportedLocales.add(getLocale("fr"));
		supportedLocales.add(getLocale("de"));
		supportedLocales.add(getLocale("it"));
		supportedLocales.add(getLocale("ja"));
		supportedLocales.add(getLocale("no"));
		supportedLocales.add(getLocale("pl"));
		supportedLocales.add(getLocale("ru"));
		supportedLocales.add(getLocale("es"));
		supportedLocales.add(getLocale("sv"));
		supportedLocales.add(getLocale("pt"));
		
	}

}
