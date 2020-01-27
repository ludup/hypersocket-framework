package com.hypersocket.jobs;

import com.hypersocket.tables.Column;

public enum JobResourceColumns implements Column {

	NAME;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}
