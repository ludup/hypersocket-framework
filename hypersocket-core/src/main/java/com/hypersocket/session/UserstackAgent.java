package com.hypersocket.session;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserstackAgent implements Serializable {
	
	private static final long serialVersionUID = -1950693512544797363L;

	String ua;
	String type;
	String brand;
	String name;
	String url;
	
	UserstackOS os = new UserstackOS();
	UserstackDevice device = new UserstackDevice();
	UserstackBrowser browser = new UserstackBrowser();
	UserstackCrawler crawler = new UserstackCrawler();
	
	public UserstackAgent() {
		
	}
	
	public String getUa() {
		return ua;
	}
	public void setUa(String ua) {
		this.ua = ua;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public UserstackOS getOs() {
		return os;
	}
	public void setOs(UserstackOS os) {
		this.os = os;
	}
	public UserstackDevice getDevice() {
		return device;
	}
	public void setDevice(UserstackDevice device) {
		this.device = device;
	}
	public UserstackBrowser getBrowser() {
		return browser;
	}
	public void setBrowser(UserstackBrowser browser) {
		this.browser = browser;
	}
	public UserstackCrawler getCrawler() {
		return crawler;
	}
	public void setCrawler(UserstackCrawler crawler) {
		this.crawler = crawler;
	}
	
	
	
}
