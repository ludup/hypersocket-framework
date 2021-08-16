/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class HypersocketVersion {

	static Map<String,String> versions = Collections.synchronizedMap(new HashMap<>());
	
	public static String getVersion() {
		return getVersion("hypersocket-common");
	}
	
	public static String getSerial() {
		Preferences pref = Preferences.userNodeForPackage(HypersocketVersion.class);
		
		String hypersocketId = System.getProperty("hypersocket.id", "hypersocket-one");
		if(pref.get("hypersocket.serial", null)!=null) {
			pref.put(hypersocketId, pref.get("hypersocket.serial", UUID.randomUUID().toString()));
			pref.remove("hypersocket.serial");
		} 
		String serial = pref.get(hypersocketId, UUID.randomUUID().toString());
		pref.put(hypersocketId, serial);
		return serial;
	}
	
	public static String getVersion(String artifactId) {
		String fakeVersion = Boolean.getBoolean("hypersocket.development") ? 
				System.getProperty("hypersocket.development.version", System.getProperty("hypersocket.devVersion")) : null;
		if(fakeVersion != null) {
			return fakeVersion;
		}

	    String detectedVersion = versions.get(artifactId);
	    if(detectedVersion != null)
	    	return detectedVersion;

	    // try to load from maven properties first
	    try {
	        Properties p = new Properties();
	        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/maven/com.hypersocket/" + artifactId + "/pom.properties");
	        if(is == null) {
		        is = HypersocketVersion.class.getResourceAsStream("/META-INF/maven/com.hypersocket/" + artifactId + "/pom.properties");
	        }
	        if (is != null) { 
	            p.load(is);
	            detectedVersion = p.getProperty("version", "");
	        }
	    } catch (Exception e) {
	        // ignore
	    }

	    // fallback to using Java API
	    if (detectedVersion == null) {
	        Package aPackage = HypersocketVersion.class.getPackage();
	        if (aPackage != null) {
	            detectedVersion = aPackage.getImplementationVersion();
	            if (detectedVersion == null) {
	                detectedVersion = aPackage.getSpecificationVersion();
	            }
	        }
	    }

	    if (detectedVersion == null) {
	    	try {
	    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	            Document doc = docBuilder.parse (new File("pom.xml"));
	            detectedVersion = doc.getDocumentElement().getElementsByTagName("version").item(0).getTextContent();
	    	} catch (Exception e) {
				detectedVersion = "DEV_VERSION";
			} 
	        
	    }
	    
	    versions.put(artifactId, detectedVersion);

	    return detectedVersion;
	}

	public static String getProductId() {
		return System.getProperty("hypersocket.id", "hypersocket-one");
	} 
	
	public static String getBrandId() {
		String id = getProductId();
		int idx = id.indexOf('-');
		if(idx==-1) {
			throw new IllegalStateException("Product id must consist of string formatted like <brand>-<product>");
		}
		return id.substring(0, idx);
	} 
}
