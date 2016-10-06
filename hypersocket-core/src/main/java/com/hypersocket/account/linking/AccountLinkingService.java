package com.hypersocket.account.linking;

import java.util.Collection;

import org.quartz.SchedulerException;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface AccountLinkingService extends AuthenticatedService {

	void linkAccounts(Principal primary, Principal secondary) throws ResourceException, AccessDeniedException;

	void unlinkAccounts(Principal primary, Principal secondary) throws ResourceException, AccessDeniedException;

	Collection<Principal> getLinkedAccounts(Principal primary) throws ResourceException, AccessDeniedException;

	boolean hasLinkedAccount(Realm secondaryRealm, Principal primaryAccount) throws ResourceException, AccessDeniedException;

	Principal getLinkedAccount(Realm secondaryRealm, Principal primaryAccount) throws ResourceException, AccessDeniedException;

	AccountLinkingRules getSecondaryRules(Realm realm);

	AccountLinkingRules getPrimaryRules(Realm primary, Realm secondary) throws ResourceNotFoundException;

	Collection<AccountLinkingRules> getPrimaryRules(Realm realm);

	void enableLinking(Realm primary, Realm secondary, AccountLinkingRules rules, boolean performBulkOperation) throws SchedulerException;

	void disableLinking(Realm primary, Realm secondary, boolean performBulkOperation) throws SchedulerException;

	boolean isLinking(Realm secondaryRealm);

	void performBulkAssignment(Realm realm, Collection<Principal> principals);

	void performBulkUnassignment(Realm realm, Collection<Principal> principals);

}
