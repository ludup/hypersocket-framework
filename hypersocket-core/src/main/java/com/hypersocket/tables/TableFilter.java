package com.hypersocket.tables;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public interface TableFilter {

	String getResourceKey();

	@JsonIgnore
	<T> List<T> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting);

	@JsonIgnore
	Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern);
	
	boolean getUseDefaultColumns();
	
	Collection<SearchColumn> getSearchColumns();

	@JsonIgnore
	List<?> searchPersonalResources(Principal principal, String searchColumn, String searchPattern,
			int start, int length, ColumnSort[] sorting);

	@JsonIgnore
	Long searchPersonalResourcesCount(Principal principal, String searchColumn, String searchPattern);
	
	default boolean isEnabled(Realm realm) {
		return true;
	}

}
