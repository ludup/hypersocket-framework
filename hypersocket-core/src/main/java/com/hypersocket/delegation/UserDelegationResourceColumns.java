package com.hypersocket.delegation;

import com.hypersocket.tables.Column;

public enum UserDelegationResourceColumns implements Column {

	NAME;

	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}