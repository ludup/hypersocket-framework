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
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.Manifest;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

public class HypersocketVersion {

	private static String version;
	
	public static String getVersion() {
		if(Boolean.getBoolean("hypersocket.development")) {
			return System.getProperty("hypersocket.devVersion", getVersion("hypersocket-common"));
		}
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
		String fakeVersion = System.getProperty("hypersocket.development.version");
		if(fakeVersion != null) {
			return fakeVersion;
		}
		
	    if (version != null) {
	        return version;
	    }
	    
	    /* Load the MANIFEST.MF from all jars looking for the X-Extension-Version
	     * attribute. Any jar that has the attribute also can optionally have
	     * an X-Extension-Priority attribute. The highest priority is the
	     * version that will be used.
	     */
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    if(cl == null)
	    	cl = HypersocketVersion.class.getClassLoader();
		try {
			int highestPriority = -1;
			String highestPriorityVersion = null;
		    for(Enumeration<URL> en = cl.getResources("META-INF/MANIFEST.MF");
		    		en.hasMoreElements(); ) {
		    	URL url = en.nextElement();
		    	try(InputStream in = url.openStream()) {
			    	Manifest mf = new Manifest(in);	
			    	String extensionVersion = mf.getMainAttributes().getValue("X-Extension-Version");
			    	if(StringUtils.isNotBlank(extensionVersion)) {
				    	String priorityStr = mf.getMainAttributes().getValue("X-Extension-Priority");
				    	int priority = StringUtils.isBlank(priorityStr) ? 0 : Integer.parseInt(priorityStr);
				    	if(priority > highestPriority) {
				    		highestPriorityVersion = extensionVersion;
				    	}
			    	}
		    	}
		    }
		    if(highestPriorityVersion != null)
		    	version = highestPriorityVersion;
	    }
	    catch(Exception e) {
	    	
	    }

	    // try to load from maven properties first
	    try {
	        Properties p = new Properties();
	        InputStream is = cl.getResourceAsStream("META-INF/maven/com.hypersocket/" + artifactId + "/pom.properties");
	        if(is == null) {
		        is = HypersocketVersion.class.getResourceAsStream("/META-INF/maven/com.hypersocket/" + artifactId + "/pom.properties");
	        }
	        if (is != null) {
	            p.load(is);
	            version = p.getProperty("version", "");
	        }
	    } catch (Exception e) {
	        // ignore
	    }

	    // fallback to using Java API
	    if (version == null) {
	        Package aPackage = HypersocketVersion.class.getPackage();
	        if (aPackage != null) {
	            version = aPackage.getImplementationVersion();
	            if (version == null) {
	                version = aPackage.getSpecificationVersion();
	            }
	        }
	    }

	    if (version == null) {
	    	try {
	    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	            Document doc = docBuilder.parse (new File("pom.xml"));
	            version = doc.getDocumentElement().getElementsByTagName("version").item(0).getTextContent();
	    	} catch (Exception e) {
				version = "DEV_VERSION";
			} 
	        
	    }
	    
	    /* Treat snapshot versions as build zero */
	    if(version.endsWith("-SNAPSHOT")) {
	    	version = version.substring(0, version.length() - 9) + "-0";
	    }

	    return version;
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
