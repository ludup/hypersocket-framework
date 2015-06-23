package com.hypersocket.attributes.user;

import com.hypersocket.tables.Column;

public enum UserAttributeCategoryColumns implements Column {

	NAME;

	public String getColumnName() {
		switch (this.ordinal()) {
		default:
			return "name";
		}
	}
}
