package com.hypersocket.attributes.role;

import com.hypersocket.tables.Column;

public enum RoleAttributeColumns implements Column {

	NAME;

	public String getColumnName() {
		switch (this.ordinal()) {
		default:
			return "name";
		}
	}
}
