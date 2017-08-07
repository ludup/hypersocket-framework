package com.hypersocket.tables;

public class SearchColumn {

	String url;
	String resourceKey;
	String column;
	
	public SearchColumn() {
		
	}
	
	public SearchColumn(String url, String resourceKey, String column) {
		super();
		this.url = url;
		this.resourceKey = resourceKey;
		this.column = column;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getResourceKey() {
		return resourceKey;
	}
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	
	
}
