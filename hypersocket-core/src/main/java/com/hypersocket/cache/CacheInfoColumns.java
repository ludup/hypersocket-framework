package com.hypersocket.cache;

import com.hypersocket.tables.Column;

public enum CacheInfoColumns implements Column {

	ID, HITS, HIT_PERCENT, MISSES, MISS_PERCENT, SIZE, LAST_ACCESS;

	public String getColumnName() {
		return name().toLowerCase();
	}
}