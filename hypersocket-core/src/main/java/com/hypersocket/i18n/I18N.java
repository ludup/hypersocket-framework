/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import com.hypersocket.util.FileUtils;

public class I18N {

	static File overideDirector = new File(System.getProperty(
			"hypersocket.conf", "conf"), "i18n");
	static Map<File, Properties> overideProperties = new HashMap<File, Properties>();

	private I18N() {
	}

	public static ResourceBundle getResourceBundle(Locale locale,
			String resourceBundle) {
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle");
		}
		if (!resourceBundle.startsWith("i18n/")) {
			resourceBundle = "i18n/" + resourceBundle;
		}

		return ResourceBundle.getBundle(resourceBundle, locale,
				I18N.class.getClassLoader());

	}
	
	public static void overrideMessage(Locale locale, Message message) {
		
		File overrideFile = getOverrideFile(locale, message.getBundle());
		if (!overideProperties.containsKey(overrideFile)) {
			overideProperties.put(overrideFile, new Properties());
			
			if(overrideFile.exists()) {
				Properties p = overideProperties.get(overrideFile);
				try {
					p.load(new FileInputStream(overrideFile));
				} catch (IOException e) {
				}
			}
		}
		
		Properties properties = overideProperties.get(overrideFile);
		properties.put(message.getId(), message.getTranslated());

	}
	
	public static void flushOverrides() throws IOException {
		
		for(File f : overideProperties.keySet()) {
			Properties properties = overideProperties.get(f);
			f.getParentFile().mkdirs();
			f.createNewFile();
			FileOutputStream out = new FileOutputStream(f);
			try {
				properties.store(out, "Hypersocket message bundle override file");
			} finally {
				FileUtils.closeQuietly(out);
			}
		}
	}
	
	private static File getOverrideFile(Locale locale, String bundle) {
		if (locale.equals(Locale.ENGLISH)) {
			return new File(overideDirector, bundle
					+ ".properties");
		} else {
			return new File(overideDirector, bundle + "_"
					+ locale.toString() + ".properties");
		}
	}
	public static String getResource(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle for key " + key);
		}

		File overideFile = getOverrideFile(locale, resourceBundle);
		

		if (overideFile.exists()) {

			if (!overideProperties.containsKey(overideFile)) {

				Properties properties = new Properties();
				try {
					InputStream in = new FileInputStream(overideFile);
					try {
						properties.load(in);
					} catch (IOException ex) {
					} finally {
						FileUtils.closeQuietly(in);
					}
					
					overideProperties.put(overideFile, properties);
				} catch (FileNotFoundException e) {
					
				}
			}
			
			if(overideProperties.containsKey(overideFile)) {
				Properties properties = overideProperties.get(overideFile);
				
				if(properties.containsKey(key)) {
					String localizedString = properties.getProperty(key);
					if (arguments == null || arguments.length == 0) {
						return localizedString;
					}

					MessageFormat messageFormat = new MessageFormat(localizedString);
					messageFormat.setLocale(locale);
					return messageFormat.format(formatParameters(arguments));
				}
			}
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
			throw new IllegalArgumentException("Missing resource in "
					+ resourceBundle + " for key " + key);
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
			throw new IllegalArgumentException("Missing resource in "
					+ resourceBundle + " for key " + key);
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

		File overideFile = getOverrideFile(locale, resourceBundle);
		

		if (overideFile.exists()) {

			if (!overideProperties.containsKey(overideFile)) {

				Properties properties = new Properties();
				try {
					InputStream in = new FileInputStream(overideFile);
					try {
						properties.load(in);
					} catch (IOException ex) {
					} finally {
						FileUtils.closeQuietly(in);
					}
					
					overideProperties.put(overideFile, properties);
				} catch (FileNotFoundException e) {
					
				}
			}
			
			if(overideProperties.containsKey(overideFile)) {
				Properties properties = overideProperties.get(overideFile);
				
				return properties.containsKey(key);
			}
		}
		
		return false;
	}
}
