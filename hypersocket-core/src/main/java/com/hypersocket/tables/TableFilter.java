package com.hypersocket.tables;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Realm;

public interface TableFilter {

	String getResourceKey();

	@JsonIgnore
	List<?> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting);

	@JsonIgnore
	Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern);
	
	boolean getUseDefaultColumns();
	
	Collection<SearchColumn> getSearchColumns();

}
