package com.hypersocket.tables;

import java.util.Collection;
import java.util.Collections;

public abstract class DefaultTableFilter implements TableFilter {

	@Override
	public boolean getUseDefaultColumns() {
		return true;
	}

	@Override
	public Collection<SearchColumn> getSearchColumns() {
		return Collections.<SearchColumn>emptyList();
	}

}
