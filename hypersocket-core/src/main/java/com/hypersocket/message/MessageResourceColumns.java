package com.hypersocket.message;

import com.hypersocket.tables.Column;

public enum MessageResourceColumns implements Column {

	NAME;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}