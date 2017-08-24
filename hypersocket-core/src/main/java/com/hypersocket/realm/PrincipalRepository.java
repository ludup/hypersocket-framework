package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;

import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tables.ColumnSort;

public interface PrincipalRepository extends AbstractResourceRepository<Principal> {

	List<Principal> search(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting);

	long getResourceCount(Realm realm, PrincipalType type);
	
	long getResourceCount(Collection<Realm> realm, PrincipalType type);
	
	long getResourceCount(Realm realm, PrincipalType type, String searchColumn, String searchPattern);

	Collection<Principal> getPrincpalsByName(String username, PrincipalType... types);

	Collection<Principal> getPrincpalsByName(String username, Realm realm, PrincipalType... types);

	Collection<Principal> allPrincipals();

}
