package com.hypersocket.realm;

import java.util.List;

import com.hypersocket.tables.ColumnSort;

public interface PrincipalFilter {

	String getResourceKey();

	List<?> getPrincipals(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting);

	Long getPrincipalCount(Realm realm, PrincipalType type, String searchColumn, String searchPattern);

}
