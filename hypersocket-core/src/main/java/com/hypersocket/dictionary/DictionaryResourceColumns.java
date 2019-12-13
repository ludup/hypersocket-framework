package com.hypersocket.dictionary;

import com.hypersocket.tables.Column;

public enum DictionaryResourceColumns implements Column {

	LOCALE, TEXT;
	
	public String getColumnName() {
		return name().toLowerCase();
	}
}