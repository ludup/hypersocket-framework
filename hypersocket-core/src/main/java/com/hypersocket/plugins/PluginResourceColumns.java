package com.hypersocket.plugins;

import com.hypersocket.tables.Column;

public enum PluginResourceColumns implements Column {

	ID, DESCRIPTION, VERSION, VENDOR, PROVIDER, STATE;
	
	public String getColumnName() {
		return name().toLowerCase();
	}
}