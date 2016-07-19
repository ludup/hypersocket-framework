package com.hypersocket.account.linking;

import java.util.Collection;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;

public interface AccountLinkingService extends AuthenticatedService {

	void linkAccounts(Principal primary, Principal secondary) throws ResourceException, AccessDeniedException;

	void unlinkAccounts(Principal primary, Principal secondary) throws ResourceException, AccessDeniedException;

	Collection<Principal> getLinkedAccounts(Principal primary) throws ResourceException, AccessDeniedException;

	boolean hasLinkedAccount(Realm secondaryRealm, Principal primaryAccount) throws ResourceException, AccessDeniedException;

	Principal getLinkedAccount(Realm secondaryRealm, Principal primaryAccount) throws ResourceException, AccessDeniedException;

}
