package com.hypersocket.realm;

import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public class RealmAdapter implements RealmListener {

	@Override
	public void onCreateRealm(Realm realm) throws ResourceCreationException {

	}

	@Override
	public void onUpdateRealm(Realm realm) throws ResourceChangeException {

	}

	@Override
	public void onDeleteRealm(Realm realm) throws ResourceChangeException {

	}

	@Override
	public boolean hasCreatedDefaultResources(Realm realm) {
		return true;
	}

}
