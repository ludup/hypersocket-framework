package com.hypersocket.realm;

import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface RealmListener {

	public boolean hasCreatedDefaultResources(Realm realm);
	
	public void onCreateRealm(Realm realm) throws ResourceCreationException;
	
	public void onUpdateRealm(Realm realm) throws ResourceChangeException;
	
	public void onDeleteRealm(Realm realm) throws ResourceChangeException;
}
