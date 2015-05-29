package com.hypersocket.browser;

import com.hypersocket.tables.Column;

public enum BrowserLaunchableColumns implements Column {

	NAME, LOGO, TYPE;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		case 2:
			return "type";
		case 1:
			return "logo";
		default:
			return "name";
		}
	}
}
