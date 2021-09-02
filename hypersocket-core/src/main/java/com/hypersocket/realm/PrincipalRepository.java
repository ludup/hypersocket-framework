package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.ResourceChangeException;
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

	Principal getPrincipalByReference(String reference, Realm realm);

	List<?> searchDeleted(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			ColumnSort[] sorting, int start, int length, boolean local, CriteriaConfiguration... criteriaConfiguration);

	Long getDeletedCount(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			boolean local, CriteriaConfiguration... criteriaConfiguration);
	
	List<?> searchSuspendedState(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			ColumnSort[] sorting, int start, int length, boolean suspended, CriteriaConfiguration... criteriaConfiguration);
	
	Long getSuspendedStateCount(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			boolean suspended, CriteriaConfiguration... criteriaConfiguration);
	
	List<?> searchRemoteUserWithPrincipalStatus(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			ColumnSort[] sorting, int start, int length, List<PrincipalStatus> principalStatuses, CriteriaConfiguration... criteriaConfiguration);
	
	Long getRemoteUserWithPrincipalStatusCount(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			List<PrincipalStatus> principalStatuses, CriteriaConfiguration... criteriaConfiguration);

	void undelete(Realm realm, Principal user) throws ResourceChangeException;

}
