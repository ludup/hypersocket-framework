/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.ApplicationContextServiceImpl;

public class I18N {

	static Logger log = LoggerFactory.getLogger(I18N.class);
	
	private I18N() {
	}
	
	public static boolean bundleExists(String resourceBundle) {

		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle");
		}
		
		String bundle = resourceBundle;
		if (!bundle.startsWith("i18n/")) {
			bundle = "i18n/" + resourceBundle;
		}

		try {
			ResourceBundle.getBundle(bundle, Locale.getDefault(),
					I18N.class.getClassLoader());
			return true;
		} catch (MissingResourceException e) {
		}
		return false;
	}

	public static Set<String> getResourceKeys(Locale locale,
			String resourceBundle) {
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle");
		}
		
		String bundle = resourceBundle;
		if (!bundle.startsWith("i18n/")) {
			bundle = "i18n/" + resourceBundle;
		}

		Set<String> keys = new HashSet<String>();
		try {
			ResourceBundle rb = ResourceBundle.getBundle(bundle, locale,
					I18N.class.getClassLoader());
			keys.addAll(rb.keySet());
		} catch (MissingResourceException e) {
		}
		
		I18nOverrideRepository repository = ApplicationContextServiceImpl.getInstance().getBean(I18nOverrideRepository.class);
		
		if(repository.hasResources(locale, resourceBundle)) {
			if(log.isInfoEnabled()) {
				log.info("i18n overrides exist in bundle " + resourceBundle);
			}
			keys.addAll(repository.getResourceKeys(locale, resourceBundle));
		} 

		return keys;
	}
	
	public static void overrideMessage(Locale locale, Message message) {
		
		ApplicationContextServiceImpl.getInstance()
		.getBean(I18nOverrideRepository.class)
			.createResource(locale, message.getBundle(), message.getId(), message.getTranslated());

	}
	
	public static void removeOverrideMessage(Locale locale, Message message) {		
		ApplicationContextServiceImpl.getInstance()
			.getBean(I18nOverrideRepository.class)
				.deleteResource(locale, message.getBundle(), message.getId());
	}

	public static String getResourceOrException(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle for key " + key);
		}

		I18nOverride override = null;
		try {
			override = ApplicationContextServiceImpl.getInstance()
					.getBean(I18nOverrideRepository.class)
						.getResource(locale, resourceBundle, key);
		}
		catch(Exception e) {
			log.warn("Could check for localisation override.", e);
		}

		if (override!=null) {
			String localizedString = override.getValue();
			if (arguments == null || arguments.length == 0) {
				return localizedString;
			}

			MessageFormat messageFormat = new MessageFormat(localizedString);
			messageFormat.setLocale(locale);
			return messageFormat.format(formatParameters(arguments));
		}
		
		String bundlePath = resourceBundle;
		if (!bundlePath.startsWith("i18n/")) {
			bundlePath = "i18n/" + resourceBundle;
		}
		
		ResourceBundle resource = ResourceBundle.getBundle(bundlePath,
				locale, I18N.class.getClassLoader());
		String localizedString = resource.getString(key);
		if (arguments == null || arguments.length == 0) {
			return localizedString;
		}

		MessageFormat messageFormat = new MessageFormat(localizedString);
		messageFormat.setLocale(locale);
		return messageFormat.format(formatParameters(arguments));
	}

	public static String getResource(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		try {
			return getResourceOrException(locale, resourceBundle, key, arguments);
		} catch (MissingResourceException mre) {
			return "Missing resource key [i18n/" + resourceBundle + "/" + key + "]";
		}
	}
	
	public static String getResourceNoOveride(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle for key " + key);
		}
		
		if (!resourceBundle.startsWith("i18n/")) {
			resourceBundle = "i18n/" + resourceBundle;
		}
		
		try {
			ResourceBundle resource = ResourceBundle.getBundle(resourceBundle,
					locale, I18N.class.getClassLoader());
			String localizedString = resource.getString(key);
			if (arguments == null || arguments.length == 0) {
				return localizedString;
			}

			MessageFormat messageFormat = new MessageFormat(localizedString);
			messageFormat.setLocale(locale);
			return messageFormat.format(formatParameters(arguments));
		} catch (MissingResourceException mre) {
			return "[i18n/" + resourceBundle + "/" + key + "]";
		}
	}

	private static Object[] formatParameters(Object... arguments) {
		Collection<Object> formatted = new ArrayList<Object>(arguments.length);
		for (Object arg : arguments) {
			if (arg instanceof Date) {
				formatted.add(DateFormat.getDateTimeInstance().format(arg));
			} else {
				formatted.add(arg);
			}
		}
		return formatted.toArray(new Object[formatted.size()]);
	}
	
	public static boolean hasOveride(Locale locale, String resourceBundle,
			String key) {
		
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle for key " + key);
		}

		I18nOverride override = ApplicationContextServiceImpl.getInstance()
				.getBean(I18nOverrideRepository.class)
					.getResource(locale, resourceBundle, key);
		return override!=null;
	}
}
