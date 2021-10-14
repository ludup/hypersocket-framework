package com.hypersocket.realm;

import java.util.List;

import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tables.ColumnSort;

public interface UserPrincipalRepository extends AbstractResourceRepository<UserPrincipal<GroupPrincipal<?,?>>> {
	
	List<UserPrincipal<GroupPrincipal<?,?>>> getNeverLoggedInSearch(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting);
	
	long getNeverLoggedInCount(Realm realm, String searchColumn, String searchPattern);
	
	List<UserPrincipal<GroupPrincipal<?,?>>> getNeverLoggedInDaysSearch(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, int days);

	long getNeverLoggedInDaysCount(Realm realm, String searchColumn, String searchPattern, int days);
}
