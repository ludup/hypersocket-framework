package com.hypersocket.resource;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

import java.util.List;

public interface FindableResourceRepository<T extends SimpleResource> {

    T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException;

    List<T> getResourcesByIds(Long...ids);

	void deleteRealm(Realm realm);

	boolean isDeletable();

}
