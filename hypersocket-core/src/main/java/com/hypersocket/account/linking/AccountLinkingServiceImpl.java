package com.hypersocket.account.linking;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.transactions.TransactionService;

@Service
public class AccountLinkingServiceImpl extends AbstractAuthenticatedServiceImpl implements AccountLinkingService {

	public static final String RESOURCE_BUNDLE = "AccountLinkingService";
	
	@Autowired
	I18NService i18nService; 
	
	@Autowired
	TransactionService transactionService; 
	
	@Autowired
	PrincipalRepository repository;
	
	@Autowired
	RealmService realmService;
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	
	}
	
	@Override
	public void linkAccounts(final Principal primary, final Principal secondary) throws ResourceException, AccessDeniedException {
		
		if(!primary.isPrimaryAccount()) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.notPrimaryAccount", primary.getName());
		}
		
		if(secondary.isPrimaryAccount()) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.notSecondaryAccount", secondary.getName());
		}
		
		if(secondary.getParentPrincipal()!=null) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.alreadyLinked", secondary.getName(), secondary.getParentPrincipal().getName());
		}
		
		transactionService.doInTransaction(new TransactionCallback<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus transaction) {
				
				try {
					Principal principal = realmService.getPrincipalById(primary.getRealm(), primary.getId(), PrincipalType.USER);
					
					Hibernate.initialize(principal);
					
					if(principal.getLinkedPrincipals().contains(secondary)) {
						throw new IllegalStateException(
								new ResourceCreationException(
										RESOURCE_BUNDLE, 
										"error.alreadyLinked", 
										secondary.getName(), 
										primary.getName()));
					}
					
					secondary.setParentPrincipal(principal);
					principal.getLinkedPrincipals().add(secondary);
					
					repository.savePrincipal(principal);
					repository.savePrincipal(secondary);
					
					return principal;
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e);
				}
				
				
			}
		});
	}
	
	@Override
	public void unlinkAccounts(final Principal primary, final Principal secondary) throws ResourceException, AccessDeniedException {
		
		if(!primary.isPrimaryAccount()) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.notPrimaryAccount", primary.getName());
		}
		
		if(secondary.isPrimaryAccount()) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.notSecondaryAccount", secondary.getName());
		}
		
		if(!secondary.getParentPrincipal().equals(primary)) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.notLinked", secondary.getName(), secondary.getParentPrincipal().getName());
		}
		
		transactionService.doInTransaction(new TransactionCallback<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus transaction) {
				
				try {
					Principal principal = realmService.getPrincipalById(
							primary.getRealm(), 
							primary.getId(), 
							PrincipalType.USER);
					
					Hibernate.initialize(principal);
					
					if(!principal.getLinkedPrincipals().contains(secondary)) {
						throw new IllegalStateException(
								new ResourceCreationException(
										RESOURCE_BUNDLE, 
										"error.notLinked", 
										secondary.getName(), 
										primary.getName()));
					}
					
					secondary.setParentPrincipal(null);
					principal.getLinkedPrincipals().remove(secondary);
					
					repository.savePrincipal(principal);
					repository.savePrincipal(secondary);
					
					return principal;
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}
	
	@Override
	public Collection<Principal> getLinkedAccounts(final Principal primary) throws ResourceException, AccessDeniedException {
		
		return transactionService.doInTransaction(new TransactionCallback<Collection<Principal>>() {

			@Override
			public Collection<Principal> doInTransaction(TransactionStatus transaction) {
				
				try {
					Principal principal = realmService.getPrincipalById(
							primary.getRealm(), 
							primary.getId(), 
							PrincipalType.USER);
					
					Hibernate.initialize(principal);
					return new ArrayList<Principal>(principal.getLinkedPrincipals());
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}
	
	@Override
	public boolean hasLinkedAccount(Realm secondaryRealm, Principal primaryAccount) throws ResourceException, AccessDeniedException {
		
		Collection<Principal> linkedAccounts = getLinkedAccounts(primaryAccount);
		
		for(Principal principal : linkedAccounts) {
			if(principal.getRealm().equals(secondaryRealm)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Principal getLinkedAccount(Realm secondaryRealm, Principal primaryAccount) throws ResourceException, AccessDeniedException {
		
		Collection<Principal> linkedAccounts = getLinkedAccounts(primaryAccount);
		
		for(Principal principal : linkedAccounts) {
			if(principal.getRealm().equals(secondaryRealm)) {
				return principal;
			}
		}
		
		throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.accountNotLinked", primaryAccount.getPrincipalName(), secondaryRealm.getName());
	}
}

