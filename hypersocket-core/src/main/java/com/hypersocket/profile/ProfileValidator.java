package com.hypersocket.profile;

import com.hypersocket.realm.Realm;

public interface ProfileValidator {

	int getMaximumCompletedAuths(Realm realm);

	boolean hasMaximumCompletedAuth(Realm realm);
}
