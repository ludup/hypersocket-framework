package com.hypersocket.client.rmi;

import java.awt.Desktop;
import java.io.Serializable;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebsiteResourceLauncher implements ResourceLauncher, Serializable {

	private static Logger log = LoggerFactory.getLogger(WebsiteResourceLauncher.class);
	
	private static final long serialVersionUID = 7508038649288643559L;

	WebsiteResourceTemplate website;
	
	public WebsiteResourceLauncher(WebsiteResourceTemplate website) {
		this.website = website;
	}

	@Override
	public void launch() {
		
		if(log.isInfoEnabled()) {
			log.info("Launching website " + website.getName() + " " + website.getLaunchUrl());
		}
		
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI(website.getLaunchUrl()));
	        } catch (Exception e) {
	           log.error("Failed to launch website", e);
	        }
	    }
	}

}
