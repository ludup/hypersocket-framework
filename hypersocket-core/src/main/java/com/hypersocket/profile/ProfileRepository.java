package com.hypersocket.profile;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepository;

public interface ProfileRepository extends AbstractEntityRepository<Profile, Long> {

	long getCompleteProfileCount(Realm realm);

	long getIncompleteProfileCount(Realm realm);

	long getPartiallyCompleteProfileCount(Realm realm);

}
