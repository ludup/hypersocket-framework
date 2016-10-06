package com.hypersocket.account.linking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.hibernate.Hibernate;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.account.linking.events.AccountLinkedEvent;
import com.hypersocket.account.linking.events.AccountUnlinkedEvent;
import com.hypersocket.account.linking.jobs.BulkPrincipalAssignmentUserLinkingJob;
import com.hypersocket.account.linking.jobs.BulkPrincipalUnassignmentUserUnlinkingJob;
import com.hypersocket.account.linking.jobs.BulkSecondaryUserLinkingJob;
import com.hypersocket.account.linking.jobs.BulkSecondaryUserUnlinkingJob;
import com.hypersocket.account.linking.jobs.PrimaryUserCreationEventJob;
import com.hypersocket.account.linking.jobs.PrimaryUserDeletionEventJob;
import com.hypersocket.account.linking.jobs.SecondaryUserCreationEventJob;
import com.hypersocket.account.linking.jobs.SecondaryUserDeletionEventJob;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalRepository;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.events.RealmDeletedEvent;
import com.hypersocket.realm.events.UserCreatedEvent;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.role.events.RoleCreatedEvent;
import com.hypersocket.role.events.RoleDeletedEvent;
import com.hypersocket.role.events.RoleUpdatedEvent;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.transactions.TransactionCallbackWithError;
import com.hypersocket.transactions.TransactionService;

@Service
public class AccountLinkingServiceImpl extends AbstractAuthenticatedServiceImpl implements AccountLinkingService {

	public static final String RESOURCE_BUNDLE = "AccountLinkingService";
	
	static Logger log = LoggerFactory.getLogger(AccountLinkingServiceImpl.class);
	
	@Autowired
	I18NService i18nService; 
	
	@Autowired
	TransactionService transactionService; 
	
	@Autowired
	PrincipalRepository repository;
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	SchedulerService schedulerService; 
	
	@Autowired
	EventService eventService; 
	
	Map<Realm, Collection<AccountLinkingRules>> primaryRules = new HashMap<Realm,Collection<AccountLinkingRules>>();
	Map<Realm, AccountLinkingRules> secondaryRules = new HashMap<Realm,AccountLinkingRules>();
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	
	}
	
	@Override
	public synchronized boolean isLinking(Realm secondaryRealm) {
		return secondaryRules.containsKey(secondaryRealm);
	}
	
	@Override
	public synchronized void enableLinking(Realm primary, Realm secondary, AccountLinkingRules rules, boolean performBulkOperation) throws SchedulerException {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Linking primary realm %s to secondary realm %s", 
					primary.getName(),
					secondary.getName()));
		}
		
		if(!primaryRules.containsKey(primary)) {
			primaryRules.put(primary, new ArrayList<AccountLinkingRules>());
		}
		primaryRules.get(primary).add(rules);
		secondaryRules.put(secondary, rules);
		
		if(performBulkOperation) {
			JobDataMap data = new JobDataMap();
			data.put("primaryRealmId", rules.getPrimaryRealm().getId());
			data.put("secondaryRealmId", rules.getSecondaryRealm().getId());
			
			schedulerService.scheduleNow(BulkSecondaryUserLinkingJob.class, UUID.randomUUID().toString(), data);
		}
	}
	
	@Override
	public synchronized void disableLinking(Realm primary, Realm secondary, boolean performBulkOperation) throws SchedulerException {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Unlinking primary realm %s from secondary realm %s", 
					primary.getName(),
					secondary.getName()));
		}
		
		boolean disable = false;
		if(secondaryRules.containsKey(secondary)) {
			disable = secondaryRules.get(secondary).isDisableAccountRequired();
			secondaryRules.remove(secondary);
			for(AccountLinkingRules rules : primaryRules.get(primary)) {
				if(rules.getSecondaryRealm().equals(secondary)) {
					primaryRules.get(primary).remove(secondary);
					break;
				}
			}
		}
		
		if(performBulkOperation) {
			JobDataMap data = new JobDataMap();
			data.put("primaryRealmId", primary.getId());
			data.put("secondaryRealmId", secondary.getId());
			data.put("disableAccount", new Boolean(disable));
			schedulerService.scheduleNow(BulkSecondaryUserUnlinkingJob.class, UUID.randomUUID().toString(), data);
		}
	}
	
	@Override
	public void performBulkAssignment(Realm realm, Collection<Principal> principals) {
		if(!principals.isEmpty()) {
			
			JobDataMap data = new JobDataMap();
			data.put("primaryRealmId", realm.getId());
			data.put("assignedIds", ResourceUtils.createResourceIdArray(principals));
			
			try {
				schedulerService.scheduleNow(BulkPrincipalAssignmentUserLinkingJob.class, UUID.randomUUID().toString(), data);
			} catch (SchedulerException e) {
				log.error("Failed to schedule bulk role assignment job", e);
			}
		}
	
	}

	@Override
	public void performBulkUnassignment(Realm realm, Collection<Principal> principals) {
		
		if(!principals.isEmpty()) {

			JobDataMap data = new JobDataMap();
			data.put("primaryRealmId", realm.getId());
			data.put("unassignedIds", ResourceUtils.createResourceIdArray(principals));
			
			try {
				schedulerService.scheduleNow(BulkPrincipalUnassignmentUserUnlinkingJob.class, UUID.randomUUID().toString(), data);
			} catch (SchedulerException e) {
				log.error("Failed to schedule bulk role assignment job", e);
			}
		}
	}
	
	@EventListener
	public synchronized void handleUserCreation(UserCreatedEvent event) {
		
		if(event.isSuccess()) {
			
			if(secondaryRules.containsKey(event.getPrincipal().getRealm()) && !event.getPrincipal().isLinked()) {
				AccountLinkingRules rules = secondaryRules.get(event.getPrincipal().getRealm());
				if(rules.isAutomaticLinking()) {
					if(realmService.getPrincipalByName(rules.getPrimaryRealm(), 
							rules.generatePrimaryPrincipalName(event.getPrincipal()), 
							PrincipalType.USER)!=null) {
						try {
							JobDataMap data = new JobDataMap();
							data.put("principalId", event.getPrincipal().getId());
							data.put("realmId", rules.getSecondaryRealm().getId());
							
							schedulerService.scheduleNow(SecondaryUserCreationEventJob.class, 
									UUID.randomUUID().toString(), data);
						} catch (SchedulerException e) {
							log.error("Failed to schedule secondary user unlink");
						}
					}
				}
				
			} else if(primaryRules.containsKey(event.getPrincipal().getRealm())) {
				for(AccountLinkingRules rules : getPrimaryRules(event.getPrincipal().getRealm())) {
					if(rules.isCreationEnabled()) {
						
						if(!rules.isAccountCreationRequired(event.getPrincipal())) {
							if(log.isInfoEnabled()) {
								log.info(String.format("Account linking rules for %s does not want an account created for %s",
										rules.getSecondaryRealm().getName(),
										event.getPrincipal().getName()));
							}
							return;
						}
						
						if(log.isInfoEnabled()) {
							log.info(String.format("Account linking rules for %s wants an account created for %s",
									rules.getSecondaryRealm().getName(),
									event.getPrincipal().getName()));
						}
						
						JobDataMap data = new JobDataMap();
						data.put("principalId", event.getPrincipal().getId());
						data.put("realmId", rules.getPrimaryRealm().getId());
						data.put("secondaryRealmId", rules.getSecondaryRealm().getId());
						
						try {
							schedulerService.scheduleNow(PrimaryUserCreationEventJob.class, 
									UUID.randomUUID().toString(), data);
						} catch (SchedulerException e) {
							log.error("Failed to schedule secondary user unlink");
						}
					}
				}
			}
		}
	}
	
	@EventListener
	public synchronized void handleUserDeletion(UserDeletedEvent event) {
		
		if(event.isSuccess()) {
			
			JobDataMap data = new JobDataMap();
			data.put("realmId", event.getPrincipal().getRealm().getId());
			data.put("principalId", event.getPrincipal().getId());
			
			if(secondaryRules.containsKey(event.getPrincipal().getRealm())) {
				
				AccountLinkingRules rules = secondaryRules.get(event.getPrincipal().getRealm());
				
				if(rules.isDeletionEnabled() || rules.isDisableAccountRequired()) {
					try {
						schedulerService.scheduleNow(SecondaryUserDeletionEventJob.class, 
								UUID.randomUUID().toString(), data);
					} catch (SchedulerException e) {
						log.error("Failed to schedule secondary user unlink");
					}
				}
			} else if(primaryRules.containsKey(event.getPrincipal().getRealm())) {
				
				try {
					schedulerService.scheduleNow(PrimaryUserDeletionEventJob.class, 
							UUID.randomUUID().toString(), data);
				} catch (SchedulerException e) {
					log.error("Failed to schedule primary user unlink");
				}	
			}
		}
	}
	
	@EventListener
	public synchronized void handleRealmDeletion(RealmDeletedEvent event) {
		
		if(primaryRules.containsKey(event.getRealm())) {
			for(AccountLinkingRules rules : primaryRules.get(event.getRealm())) {
				secondaryRules.remove(rules.getSecondaryRealm());
			}
			primaryRules.remove(event.getRealm());
		}
		
		if(secondaryRules.containsKey(event.getRealm())) {
			Realm primary = secondaryRules.get(event.getRealm()).getPrimaryRealm();
			try {
				disableLinking(primary, event.getRealm(), true);
			} catch (SchedulerException e) {
				log.error("Failed to schedule bulk unlink operation", e);
			}
		}
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
		
		transactionService.doInTransaction(new TransactionCallbackWithError<Principal>() {

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
					
					repository.saveResource(principal);
					repository.saveResource(secondary);
					
					eventService.publishEvent(new AccountLinkedEvent(this, getCurrentSession(), principal.getRealm(), 
							realmService.getProviderForRealm(principal.getRealm()), principal, secondary));
					
					return principal;
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e);
				}
				
				
			}

			@Override
			public void doTransacationError(Throwable e) {
				eventService.publishEvent(new AccountLinkedEvent(this, getCurrentSession(), primary.getRealm(), 
						realmService.getProviderForRealm(primary.getRealm()), primary, secondary));
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
		
		transactionService.doInTransaction(new TransactionCallbackWithError<Principal>() {

			@Override
			public Principal doInTransaction(TransactionStatus transaction) {
				
				try {
					Principal primaryPrincipal;
					
					if(primary.isDeleted()) {
						primaryPrincipal = realmService.getDeletedPrincipalById(
							primary.getRealm(), 
							primary.getId(), 
							PrincipalType.USER);
					} else {
						primaryPrincipal = realmService.getPrincipalById(
								primary.getRealm(), 
								primary.getId(), 
								PrincipalType.USER);
					}
					
					Hibernate.initialize(primaryPrincipal);
					
					if(!primaryPrincipal.getLinkedPrincipals().contains(secondary)) {
						throw new IllegalStateException(
								new ResourceCreationException(
										RESOURCE_BUNDLE, 
										"error.notLinked", 
										secondary.getName(), 
										primary.getName()));
					}
					
					Principal secondaryPrincipal;
					
					if(secondary.isDeleted()) {
						secondaryPrincipal = realmService.getDeletedPrincipalById(
								secondary.getRealm(), 
								secondary.getId(), 
								PrincipalType.USER);
					} else {
						secondaryPrincipal = realmService.getPrincipalById(
								secondary.getRealm(), 
								secondary.getId(), 
								PrincipalType.USER);
					}
					
					secondaryPrincipal.setParentPrincipal(null);
					primaryPrincipal.getLinkedPrincipals().remove(secondaryPrincipal);
					
					repository.saveResource(primaryPrincipal);
					repository.saveResource(secondaryPrincipal);
					
					eventService.publishEvent(new AccountUnlinkedEvent(this, getCurrentSession(), primaryPrincipal.getRealm(), 
							realmService.getProviderForRealm(primaryPrincipal.getRealm()), primaryPrincipal, secondaryPrincipal));
					
					return primaryPrincipal;
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e);
				}
			}

			@Override
			public void doTransacationError(Throwable e) {
				eventService.publishEvent(new AccountUnlinkedEvent(this, getCurrentSession(), primary.getRealm(), 
						realmService.getProviderForRealm(primary.getRealm()), primary, secondary, e));
			}
		});
	}
	
	@Override
	public Collection<Principal> getLinkedAccounts(final Principal primary) throws ResourceException, AccessDeniedException {
		
		return transactionService.doInTransaction(new TransactionCallback<Collection<Principal>>() {

			@Override
			public Collection<Principal> doInTransaction(TransactionStatus transaction) {
				
				try {
					Principal principal;
					
					if(primary.isDeleted()) {
						 principal = realmService.getDeletedPrincipalById(
									primary.getRealm(), 
									primary.getId(), 
									PrincipalType.USER);
					} else {
						 principal = realmService.getPrincipalById(
								primary.getRealm(), 
								primary.getId(), 
								PrincipalType.USER);
					}
					
					Hibernate.initialize(principal);
					return new ArrayList<Principal>(principal.getLinkedPrincipals());
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}
	
	@Override
	public boolean hasLinkedAccount(Realm targetRealm, Principal sourceAccount) throws ResourceException, AccessDeniedException {
		
		Collection<Principal> linkedAccounts = getLinkedAccounts(sourceAccount);
		
		for(Principal principal : linkedAccounts) {
			if(principal.getRealm().equals(targetRealm)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Principal getLinkedAccount(Realm targetRealm, Principal sourceAccount) throws ResourceException, AccessDeniedException {
		
		Collection<Principal> linkedAccounts = getLinkedAccounts(sourceAccount);
		
		for(Principal principal : linkedAccounts) {
			if(principal.getRealm().equals(targetRealm)) {
				return principal;
			}
		}
		
		throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.accountNotLinked", sourceAccount.getPrincipalName(), targetRealm.getName());
	}
	
	@Override
	public AccountLinkingRules getSecondaryRules(Realm realm) {
		return secondaryRules.get(realm);
	}
	
	@Override
	public Collection<AccountLinkingRules> getPrimaryRules(Realm realm) {
		Collection<AccountLinkingRules> rules =  primaryRules.get(realm);
		if(rules==null) {
			rules = Collections.<AccountLinkingRules>emptyList();
		}
		return rules;
	}
	
	@Override
	public AccountLinkingRules getPrimaryRules(Realm primary, Realm secondary) throws ResourceNotFoundException {
		for(AccountLinkingRules rule :getPrimaryRules(primary)) {
			if(rule.getSecondaryRealm().equals(secondary)) {
				return rule;
			}
		}
		
		throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.realmsNotLinked", primary.getName(), secondary.getName());
	}
}

