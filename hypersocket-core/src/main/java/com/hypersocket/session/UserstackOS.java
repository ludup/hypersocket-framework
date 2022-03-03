package com.hypersocket.session;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserstackOS implements Serializable {

	private static final long serialVersionUID = 120635078802333962L;

	String name;
	String code;
	String url;
	String family;
	String family_code;
	String family_vendor;
	String icon;
	String icon_large;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getFamily_code() {
		return family_code;
	}
	public void setFamily_code(String family_code) {
		this.family_code = family_code;
	}
	public String getFamily_vendor() {
		return family_vendor;
	}
	public void setFamily_vendor(String family_vendor) {
		this.family_vendor = family_vendor;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getIcon_large() {
		return icon_large;
	}
	public void setIcon_large(String icon_large) {
		this.icon_large = icon_large;
	}
	
	
}
