package com.hypersocket.session;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserstackCrawler implements Serializable {

	private static final long serialVersionUID = -9086248643846826343L;

	boolean is_crawler;
	String category;
	
	public boolean isIs_crawler() {
		return is_crawler;
	}
	public void setIs_crawler(boolean is_crawler) {
		this.is_crawler = is_crawler;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	
}
