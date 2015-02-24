package com.hypersocket.browser;

import com.hypersocket.tables.Column;

public enum BrowserLaunchableColumns implements Column {

	NAME;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}