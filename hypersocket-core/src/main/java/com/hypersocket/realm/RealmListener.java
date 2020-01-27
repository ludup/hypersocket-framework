package com.hypersocket.realm;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface RealmListener {

	boolean hasCreatedDefaultResources(Realm realm);
	
	void onCreateRealm(Realm realm) throws ResourceException, AccessDeniedException;
	
	void onUpdateRealm(Realm realm) throws ResourceException, AccessDeniedException;
	
	void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException;
	
	Integer getWeight();
}
