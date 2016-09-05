package com.hypersocket.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ResourceGroup extends RealmResource {

	private String resourceBundle;
	private String logo;

	public ResourceGroup() {
	}

	public ResourceGroup(String name, String resourceBundle, String iconText) {
		setName(name);
		setResourceBundle(resourceBundle);
		try {
			setLogo(String.format("logo/RESOURCE_GROUP/%s/96_auto_auto_%s", name, URLEncoder.encode(iconText, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(String resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
}
