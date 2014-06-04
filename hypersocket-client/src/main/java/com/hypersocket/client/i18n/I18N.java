/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.client.i18n;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.json.simple.JSONObject;

public class I18N {

	static JSONObject resources;
	static Locale locale = Locale.ENGLISH;

	private I18N() {
	}

	public static void initialize(JSONObject resources, Locale locale) {
		I18N.resources = resources;
		I18N.locale = locale;
	}

	public static String getResource(String key, Object... arguments) {
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}

		String localizedString = null;
		
		if(resources!=null) {
			localizedString = (String) resources.get(key);
		}
		if (localizedString == null) {
			
			try {
	            ResourceBundle resource = ResourceBundle.getBundle("i18n/UserInterface", locale, I18N.class.getClassLoader());
	            localizedString = resource.getString(key);
	        } catch (MissingResourceException mre) {
	        	return "i18n[" + key + "]";
	        }
			
		}
		if (arguments == null || arguments.length == 0) {
			return localizedString;
		}

		MessageFormat messageFormat = new MessageFormat(localizedString);
		messageFormat.setLocale(locale);
		return messageFormat.format(formatParameters(arguments));

	}
	
	public static String getResource(Locale locale, String resourceBundle, String key, Object... arguments) {
        if (key==null) {
            throw new IllegalArgumentException("You must specify a key!");
        }
        if (resourceBundle==null) {
            throw new IllegalArgumentException("You must specify a resource bundle for key " + key);
        }
        if (!resourceBundle.startsWith("i18n/")) {
            resourceBundle = "i18n/" + resourceBundle;
        }

        try {
            ResourceBundle resource = ResourceBundle.getBundle(resourceBundle, locale, I18N.class.getClassLoader());
            String localizedString = resource.getString(key);
            if (arguments==null || arguments.length == 0) {
                return localizedString;
            }

            MessageFormat messageFormat = new MessageFormat(localizedString);
            messageFormat.setLocale(locale);
            return messageFormat.format(formatParameters(arguments));
        } catch (MissingResourceException mre) {
            throw new IllegalArgumentException("Missing resource in " + resourceBundle + " for key " + key);
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
}
