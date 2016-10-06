package com.hypersocket.account.linking;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.resource.ResourceCreationException;

public interface AccountLinkingRules {

	/**
	 * The primary realm that the user logs into.
	 * @return
	 */
	Realm getPrimaryRealm();
	
	/**
	 * The secondary realm where an account will be linked to an account in the primary realm.
	 * @return
	 */
	Realm getSecondaryRealm();
	
	boolean isAutomaticLinking();
	
	boolean isCreationEnabled();
	
	boolean isAccountCreationRequired(Principal primaryPrincipal);
	
	boolean isDeletionEnabled();

	Principal createSecondaryPrincipal(Principal primaryPrincipal) throws ResourceCreationException, AccessDeniedException;

	String generatePrimaryPrincipalName(Principal secondaryPrincipal);

	String generateSecondaryPrincipalName(Principal primaryPrincipal);
	
	Principal getSecondaryPrincipal(Principal primaryPrincipal);

	boolean isSecondaryAccountAvailable(Principal primaryPrincipal);

	boolean isSecondaryAccountRequired(Principal primaryPrincipal);

	boolean isDisableAccountRequired();

	

}
