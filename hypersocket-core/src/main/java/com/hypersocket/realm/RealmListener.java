package com.hypersocket.realm;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface RealmListener {

	public boolean hasCreatedDefaultResources(Realm realm);
	
	public void onCreateRealm(Realm realm) throws ResourceException, AccessDeniedException;
	
	public void onUpdateRealm(Realm realm) throws ResourceException, AccessDeniedException;
	
	public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException;
	
	public Integer getWeight();
}
