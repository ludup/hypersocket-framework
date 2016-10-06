package com.hypersocket.realm;

import com.hypersocket.resource.ResourceException;

public interface RealmListener {

	public boolean hasCreatedDefaultResources(Realm realm);
	
	public void onCreateRealm(Realm realm) throws ResourceException;
	
	public void onUpdateRealm(Realm realm) throws ResourceException;
	
	public void onDeleteRealm(Realm realm) throws ResourceException;
}
