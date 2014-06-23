package com.hypersocket.template;

import com.hypersocket.tables.Column;

public enum TemplateColumns implements Column {

	NAME,
	TYPE;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		case 1:
			return "type";
		default:
			return "name";
		}
	}
}