package com.hypersocket.realm;

import com.hypersocket.resource.ResourceException;

public class RealmAdapter implements RealmListener {

	@Override
	public void onCreateRealm(Realm realm) throws ResourceException {

	}

	@Override
	public void onUpdateRealm(Realm realm) throws ResourceException {

	}

	@Override
	public void onDeleteRealm(Realm realm) throws ResourceException {

	}

	@Override
	public boolean hasCreatedDefaultResources(Realm realm) {
		return true;
	}

}
