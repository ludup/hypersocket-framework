package com.hypersocket.realm;

import java.util.List;

import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tables.ColumnSort;

public interface UserPrincipalRepository extends AbstractResourceRepository<UserPrincipal<GroupPrincipal<?,?>>> {
	
	List<UserPrincipal<GroupPrincipal<?,?>>> getNeverLoggedInSearch(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, DelegationCriteria delegationCriteria);
	
	long getNeverLoggedInCount(Realm realm, String searchColumn, String searchPattern, DelegationCriteria delegationCriteria);
	
	List<UserPrincipal<GroupPrincipal<?,?>>> getNeverLoggedInDaysSearch(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, int days, DelegationCriteria delegationCriteria);

	long getNeverLoggedInDaysCount(Realm realm, String searchColumn, String searchPattern, int days, DelegationCriteria delegationCriteria);
}
