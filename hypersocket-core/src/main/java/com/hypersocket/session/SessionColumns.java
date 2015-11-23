package com.hypersocket.session;

import com.hypersocket.tables.Column;

public enum SessionColumns implements Column {

	CREATED;

	public String getColumnName() {
		switch (this.ordinal()) {
		default:
			return "created";
		}
	}
}
