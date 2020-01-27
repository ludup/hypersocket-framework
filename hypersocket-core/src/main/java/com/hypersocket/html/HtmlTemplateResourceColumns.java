package com.hypersocket.html;

import com.hypersocket.tables.Column;

public enum HtmlTemplateResourceColumns implements Column {

	NAME;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}