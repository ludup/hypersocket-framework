package com.hypersocket.client.service.browser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonBrowserResource {

	String name;
	String launchUrl;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLaunchUrl() {
		return launchUrl;
	}
	public void setLaunchUrl(String url) {
		this.launchUrl = url;
	}
	
}
