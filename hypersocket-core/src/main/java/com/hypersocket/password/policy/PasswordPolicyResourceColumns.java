package com.hypersocket.password.policy;

import com.hypersocket.tables.Column;

public enum PasswordPolicyResourceColumns implements Column {

	NAME;

	public String getColumnName() {
		switch(this.ordinal()) {
		default:
			return "name";
		}
	}
}