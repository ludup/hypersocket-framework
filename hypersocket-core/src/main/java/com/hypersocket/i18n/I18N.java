/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

	private I18N() {
    }
    
    public static ResourceBundle getResourceBundle(Locale locale, String resourceBundle) {
        if (resourceBundle==null) {
            throw new IllegalArgumentException("You must specify a resource bundle");
        }
        if (!resourceBundle.startsWith("i18n/")) {
            resourceBundle = "i18n/" + resourceBundle;
        }
        
        return ResourceBundle.getBundle(resourceBundle, locale, I18N.class.getClassLoader());

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
