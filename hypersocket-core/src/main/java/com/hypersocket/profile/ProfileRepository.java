package com.hypersocket.profile;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepository;
import com.hypersocket.tables.ColumnSort;

public interface ProfileRepository extends AbstractEntityRepository<Profile, Long> {

	long getCompleteProfileCount(Collection<Realm>  realm);

	long getIncompleteProfileCount(Collection<Realm> realms);

	long getPartiallyCompleteProfileCount(Collection<Realm>  realm);

	Collection<Profile> getProfilesWithStatus(Collection<Realm> realm, ProfileCredentialsState...credentialsStates);

	long getCompleteProfileOnDateCount(Collection<Realm> realm, Date date);

	List<?> searchIncompleteProfiles(Realm realm, String searchColumn, String searchPattern, ColumnSort[] sorting,
			int start, int length);

	Long searchIncompleteProfilesCount(Realm realm, String searchColumn, String searchPattern);

	List<?> searchCompleteProfiles(Realm realm, String searchColumn, String searchPattern, ColumnSort[] sorting,
			int start, int length);

	Long searchCompleteProfilesCount(Realm realm, String searchColumn, String searchPattern);

	List<?> searchNeverVisitedProfiles(Realm realm, String searchColumn, String searchPattern, ColumnSort[] sorting,
			int start, int length);

	Long searchNeverVisitedProfilesCount(Realm realm, String searchColumn, String searchPattern);

	boolean hasCompletedProfile(Principal principal);
	
	boolean hasPartiallyCompletedProfile(Principal principal);
	
	boolean isPrincipalActive(Principal principal);

	void deleteRealm(Realm realm);

}
