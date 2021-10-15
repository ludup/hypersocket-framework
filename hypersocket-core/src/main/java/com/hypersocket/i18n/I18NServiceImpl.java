/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.cache.Cache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.cache.CacheService;
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
	
	private Map<String, Set<String>> bundleMap = new HashMap<>();
	private long lastUpdate = System.currentTimeMillis(); 
	private List<Locale> supportedLocales = new ArrayList<>();
	
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
		clearCache(locale, I18NGroup.DEFAULT_GROUP);
	}
	
	@Override
	public void clearCache(Locale locale, I18NGroup group) {
		lastUpdate = System.currentTimeMillis();
		cacheService.getCacheManager().destroyCache(getCacheKey(locale, group));
	}
	
	@Override
	public Set<String> getBundles() {
		return getBundles(I18NGroup.DEFAULT_GROUP);
	}
	
	@Override
	public Set<String> getBundles(I18NGroup group) {
		Set<String> bundle = bundleMap.get(group.getTitle());
		if (bundle == null) {
			return Collections.emptySet();
		}
		
		return bundle;
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
	public String getResource(String resourceKey, I18NGroup group) {
		return getResource(resourceKey, Locale.getDefault(), group);
	}
	
	@Override
	public String getResource(String resourceKey, Locale locale) {
		return getResourceMap(locale, I18NGroup.DEFAULT_GROUP).get(resourceKey);
	}
	
	@Override
	public String getResource(String resourceKey, Locale locale, I18NGroup group) {
		return getResourceMap(locale, group).get(resourceKey);
	}

	@Override
	public Cache<String,String> getResourceMap(Locale locale) {
		return getResourceMap(locale, I18NGroup.DEFAULT_GROUP);
	}
	
	@Override
	public synchronized Cache<String,String> getResourceMap(Locale locale, I18NGroup group) {
		
		String cacheKey = getCacheKey(locale, group);
		Cache<String,String> cache = cacheService.getCacheIfExists(cacheKey,String.class, String.class);

		if(cache==null) {
			cache = cacheService.getCacheOrCreate(cacheKey, String.class, String.class);
			buildCache(cache, locale, group);
		}
		return cache;
	}
	
	@Override
	public List<Locale> getSupportedLocales() {
		return supportedLocales;
	}
	
	@Override
	public Map<String,Map<String,Message>> getTranslatableMessages() {
		
		return getTranslatableMessages(I18NGroup.DEFAULT_GROUP);
	}
	
	@Override
	public Map<String,Map<String,Message>> getTranslatableMessages(I18NGroup group) {
		
		Map<String,Map<String,Message>> messages = new HashMap<String,Map<String,Message>>();
		
		Set<String> bundles = getBundles(group);
		
		if (bundles == null) {
			if (log.isWarnEnabled()) {
				log.warn(String.format("No bundle found for group %s", group));
			}
			return messages;
		}
		
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
	public boolean isRegistered(String bundle, I18NGroup group) {
		Set<String> bundleSet = bundleMap.get(group.getTitle());
		return bundleSet != null && bundleSet.contains(bundle);
	}

	@Override
	public synchronized void registerBundle(String bundle) {
		registerBundle(bundle, I18NGroup.DEFAULT_GROUP);
	}
	
	@Override
	public synchronized void registerBundle(String bundle, I18NGroup group) {
		Set<String> bundleSet = bundleMap.get(group.getTitle());
		
		if (bundleSet == null) {
			bundleSet = new HashSet<>();
		}

		if(bundleSet.contains(bundle)) {
			/* TODO: These should actually be fatal so they can be caught earlier */
			log.warn(String.format("Attempt to register bundle that has already been registered with name %s", bundle));
		}
		else {
			/* TODO: These should actually be fatal so they can be caught earlier */
			if(!I18N.bundleExists(bundle))
				log.warn(String.format("Attempt to register bundle with name %s that does not exist anywhere on the classpath (in 'i18n' resource folder).", bundle));
			else {
				bundleSet.add(bundle);
				bundleMap.put(group.getTitle(), bundleSet);
			}
		}
	}

	private void buildBundleMap(String bundle, Locale locale, Cache<String,String> resources) {
		for(String key :  I18N.getResourceKeys(locale, bundle)) {
			resources.put(key, I18N.getResource(locale, bundle, key));
		}
	}

	private void buildCache(Cache<String,String> cache, Locale locale, I18NGroup group) {
		if(log.isInfoEnabled()) {
			log.info(String.format("Building i18n resources cache for %s", locale.getLanguage()));
		}
		
		Set<String> bundles = getBundles(group);
		
		if (bundles == null) {
			if (log.isWarnEnabled()) {
				log.warn(String.format("No bundle found for group %s", group));
			}
			return;
		}
		
		for(String bundle : bundles) {
			buildBundleMap(bundle, locale, cache);
		}
		if(log.isInfoEnabled()) {
			log.info(String.format("Completed i18n resources cache for %s", locale.getLanguage()));
		}
	}


	private String getCacheKey(Locale locale, I18NGroup group) {
		return String.format("i18n-%s-%s", locale.getLanguage(), group.getTitle());
	}

	@PostConstruct
	private void postConstruct() {
		
		registerBundle(RESOURCE_BUNDLE);
		registerBundle(ConfigurationService.RESOURCE_BUNDLE);
		registerBundle(EmailNotificationService.RESOURCE_BUNDLE);
		registerBundle(PermissionService.RESOURCE_BUNDLE);
		registerBundle(RealmService.RESOURCE_BUNDLE);
		registerBundle(SessionService.RESOURCE_BUNDLE);
		registerBundle(SecretKeyServiceImpl.RESOURCE_BUNDLE);
		
		registerBundle(USER_INTERFACE_BUNDLE);
		
		
		supportedLocales.add(Locale.ENGLISH);
		try {
			Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				try(InputStream in = resources.nextElement().openStream()) {
					Manifest manifest = new Manifest(in);
					String translations = manifest.getMainAttributes().getValue("X-Translations");
					if(StringUtils.isNotBlank(translations)) {
						for(String lang : translations.split(",")) {
							supportedLocales.add(getLocale(lang.trim()));					
						}
					}
				} catch (IOException E) {
				}
			}
		}
		catch(Exception e) {
			// Ignore
		}
		if(supportedLocales.size() == 1) {
			/* DEPRECATED. The property hypersocket.translations should be set in hypersocket-core instead.
			 * That is injected to the MANIFEST.MF as read above.
			 */
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

}
