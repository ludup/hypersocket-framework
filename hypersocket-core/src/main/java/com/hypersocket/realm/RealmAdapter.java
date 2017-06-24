package com.hypersocket.realm;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public class RealmAdapter implements RealmListener {

	@Override
	public void onCreateRealm(Realm realm) throws ResourceException, AccessDeniedException {

	}

	@Override
	public void onUpdateRealm(Realm realm) throws ResourceException, AccessDeniedException {

	}

	@Override
	public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException {

	}

	@Override
	public boolean hasCreatedDefaultResources(Realm realm) {
		return true;
	}
	
	@Override
	public Integer getWeight() {
		return 0;
	}

}
