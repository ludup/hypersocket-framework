package com.hypersocket.account.linking;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceCreationException;

public interface AccountLinkingRules {

	Realm getPrimaryRealm();
	
	Realm getSecondaryRealm();
	
	boolean isAutomaticLinking();
	
	boolean isCreationEnabled();
	
	boolean isAccountCreationRequired(Principal primaryPrincipal);
	
	boolean isDeletionEnabled();

	Principal createSecondaryPrincipal(Principal primaryPrincipal) throws ResourceCreationException, AccessDeniedException;

	String generatePrimaryPrincipalName(Principal secondaryPrincipal);

	String generateSecondaryPrincipalName(Principal primaryPrincipal);

	boolean isAccountLinkedToRole();

	Principal getSecondaryPrincipal(Principal primaryPrincipal);

	boolean isSecondaryAccountAvailable(Principal primaryPrincipal);

	boolean isSecondaryAccountRequired(Principal primaryPrincipal);

	

}
