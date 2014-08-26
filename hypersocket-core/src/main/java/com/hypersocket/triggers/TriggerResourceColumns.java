package com.hypersocket.triggers;

import com.hypersocket.tables.Column;

public enum TriggerResourceColumns implements Column {

	NAME;

	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}
