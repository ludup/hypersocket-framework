package com.hypersocket.realm;

import java.util.Collection;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface LinkedAccountProvider {

	Collection<Principal> getLinkedAccounts(Principal primary) throws ResourceException, AccessDeniedException;
}
