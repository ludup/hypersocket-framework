package com.hypersocket.permissions;

import com.hypersocket.tables.Column;

public enum RoleColumns implements Column {

	NAME, TYPE;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		case 1:
			return "type";
		default:
			return "name";
		}
	}
}
