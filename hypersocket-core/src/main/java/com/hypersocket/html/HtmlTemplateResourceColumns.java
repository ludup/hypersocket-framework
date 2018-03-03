package com.hypersocket.html;

import com.hypersocket.tables.Column;

public enum HtmlTemplateResourceColumns implements Column {

	NAME;
	
	/**
	 * TODO rename this class and add any additional columns you 
	 * need to display in the resource table.
	 */
	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}