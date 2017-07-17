package com.hypersocket.password.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.AuthenticationServiceListener;
import com.hypersocket.auth.AuthenticationState;
import com.hypersocket.auth.ChangePasswordTemplate;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.input.DivField;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.local.LocalRealmProviderImpl;
import com.hypersocket.password.history.PasswordHistroyService;
import com.hypersocket.password.policy.events.PasswordPolicyResourceCreatedEvent;
import com.hypersocket.password.policy.events.PasswordPolicyResourceDeletedEvent;
import com.hypersocket.password.policy.events.PasswordPolicyResourceEvent;
import com.hypersocket.password.policy.events.PasswordPolicyResourceUpdatedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalProcessor;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractAssignableResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class PasswordPolicyResourceServiceImpl extends
		AbstractAssignableResourceServiceImpl<PasswordPolicyResource> 
			implements PasswordPolicyResourceService, PrincipalProcessor,
			AuthenticationServiceListener, PolicyResolver {

	public static final String RESOURCE_BUNDLE = "PasswordPolicyResourceService";

	static Logger log = LoggerFactory.getLogger(PasswordPolicyResourceServiceImpl.class);
	
	@Autowired
	PasswordPolicyResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	PasswordAnalyserService analyserService;
	
	@Autowired
	PasswordHistroyService passwordHistoryService; 

	@Autowired
	DictionaryService dictionaryService; 
	
	@Autowired
	AuthenticationService authenticationService; 

	
	Map<String,PolicyResolver> passwordPolicyResolvers = new HashMap<String,PolicyResolver>();
	
	public PasswordPolicyResourceServiceImpl() {
		super("passwordPolicy");
	}

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.passwordPolicy");

		for (PasswordPolicyResourcePermission p : PasswordPolicyResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("passwordPolicyResourceTemplate.xml");



		eventService.registerEvent(PasswordPolicyResourceEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(PasswordPolicyResourceCreatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(PasswordPolicyResourceUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(PasswordPolicyResourceDeletedEvent.class, RESOURCE_BUNDLE);

		EntityResourcePropertyStore.registerResourceService(PasswordPolicyResource.class, repository);
		
		authenticationService.registerListener(this);
		
		realmService.registerPrincipalProcessor(this);

		passwordPolicyResolvers.put(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY, this);
		
		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public void onCreateRealm(Realm realm) throws ResourceException {

				 try {
					 PasswordPolicyResource policy = new PasswordPolicyResource();
					 policy.setContainDictionaryWord(false);
					 policy.setContainUsername(false);
					 policy.setDN("LocalUserDefaultPolicy");
					 policy.setMaximumAge(0);
					 policy.setMaximumLength(64);
					 policy.setMinimumAge(0);
					 policy.setMinimumCriteriaMatches(4);
					 policy.setMinimumDigits(1);
					 policy.setMinimumLower(1);
					 policy.setMinimumSymbol(1);
					 policy.setMinimumUpper(1);
					 policy.setMinimumLength(8);
					 policy.setName("Local Account Default Policy");
					 policy.setPasswordHistory(0);
					 policy.setPriority(Integer.MAX_VALUE);
					 policy.setProvider(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY);
					 policy.setRealm(realm);
					 policy.setSystem(true);
					 policy.setAllowEdit(true);
					 policy.setDefaultPolicy(true);
					 policy.setValidSymbols("?!@#$%&");
					 
					createResource(policy, new HashMap<String,String>());
				} catch (AccessDeniedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				} 
			}

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return repository.getPolicyByDN("LocalUserDefaultPolicy", realm) != null;
			}
			
		});
	}

	@Override
	public void registerPolicyResolver(String resourceKey, PolicyResolver resolver) {
		passwordPolicyResolvers.put(resourceKey, resolver);
	}
	
	@Override
	protected AbstractAssignableResourceRepository<PasswordPolicyResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<?> getPermissionType() {
		return PasswordPolicyResourcePermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(PasswordPolicyResource resource) {
		eventService.publishEvent(new PasswordPolicyResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(PasswordPolicyResource resource, Throwable t) {
		eventService.publishEvent(new PasswordPolicyResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(PasswordPolicyResource resource) {
		eventService.publishEvent(new PasswordPolicyResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(PasswordPolicyResource resource, Throwable t) {
		eventService.publishEvent(new PasswordPolicyResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(PasswordPolicyResource resource) {
		eventService.publishEvent(new PasswordPolicyResourceDeletedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(PasswordPolicyResource resource, Throwable t) {
		eventService.publishEvent(new PasswordPolicyResourceDeletedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	public PasswordPolicyResource updateResource(PasswordPolicyResource resource, String name, Set<Role> roles,
			Map<String, String> properties) throws AccessDeniedException, ResourceException {

		resource.setName(name);
		
		updateResource(resource, roles, properties);

		return resource;
	}

	@Override
	public PasswordPolicyResource createResource(String name, Set<Role> roles, Realm realm,
			Map<String, String> properties) throws AccessDeniedException, ResourceException {

		PasswordPolicyResource resource = new PasswordPolicyResource();
		resource.setName(name);
		resource.setRealm(realm);
		resource.setRoles(roles);

		createResource(resource, properties);

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(PasswordPolicyResource resource)
			throws AccessDeniedException {

		assertPermission(PasswordPolicyResourcePermission.READ);
		return repository.getPropertyCategories(resource);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException {
		assertPermission(PasswordPolicyResourcePermission.READ);
		return repository.getPropertyCategories(null);
	}

	@Override
	protected Class<PasswordPolicyResource> getResourceClass() {
		return PasswordPolicyResource.class;
	}

	
	@Override
	public void beforeChangePassword(Principal principal, String newPassword, String oldPassword) throws ResourceException {
		
		if(!realmService.canChangePassword(principal)) {
			throw new ResourceChangeException(RESOURCE_BUNDLE, "error.cannotChangePassword");
		}

		try {
			PasswordPolicyResource policy = resolvePolicy(principal);
			
			if(policy.getMinimumAge() > 0) {
				// Check age
				UserPrincipal user = (UserPrincipal)principal;
				if(user.getLastPasswordChange()!=null) {
					Date changeDate = DateUtils.addDays(user.getLastPasswordChange(), 
							policy.getMinimumAge());
					if(new Date().before(changeDate)) {
						throw new ResourceChangeException(RESOURCE_BUNDLE, 
								"error.passwordPolicy.tooSoon", 
								HypersocketUtils.formatDate(changeDate, "HH:mm dd MMM yyyy"));
					}
				}
			}
			
			if(policy.getPasswordHistory() > 0) {
				// Verify history
				if(!passwordHistoryService.checkPassword(principal, newPassword, policy.getPasswordHistory())) {
					throw new ResourceChangeException(RESOURCE_BUNDLE, 
							"error.passwordHistoryViolation",
							policy.getPasswordHistory());
				}
			}
			
			validatePassword(principal.getPrincipalName(), policy, newPassword);
			
		} catch (ResourceNotFoundException e) {
			log.info(String.format("No password policy found for %s", principal.getPrincipalName()));
		}

	}

	private void validatePassword(String username, PasswordPolicyResource policy, String password) throws ResourceChangeException {
		try {
			analyserService.analyse(getCurrentLocale(), 
					username, 
					password.toCharArray(), 
					policy);
		} catch (PasswordPolicyException e) {
			switch(e.getType()) {
			case containsDictionaryWords:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.containsDictionaryWords");
			case containsUsername:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.containsUsername");
			case doesNotMatchComplexity:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.doesNotMatchComplexity");
			case notEnoughDigits:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.notEnoughDigits");
			case notEnoughLowerCase:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.notEnoughLowerCase");
			case notEnoughSymbols:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.notEnoughSymbols");
			case notEnoughUpperCase:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.notEnoughUpperCase");
			case tooLong:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.tooLong");
			case tooShort:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy.tooShort");
			default:
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.passwordPolicy");
			}
		}
	}
	
	@Override
	public void beforeUpdate(Principal principal, Map<String, String> properties) throws ResourceException {
	}

	@Override
	public void afterUpdate(Principal principal, Map<String, String> properties) throws ResourceException {
	}

	@Override
	public void beforeCreate(Realm realm, String realmModule, String username, String password, Map<String,String> properties) throws ResourceException {
		
		PasswordPolicyResource policy = repository.getDefaultPolicyByModule(realm, realmModule);
		if(policy!=null) {
			validatePassword(username, policy, password);
		}
		
	}
	
	@Override
	public void afterCreate(Principal principal, String password, Map<String, String> properties) throws ResourceException {

		passwordHistoryService.recordPassword(principal, password);
		
	}

	@Override
	public void afterChangePassword(Principal principal, String newPassword, String oldPassword) throws ResourceException {
		passwordHistoryService.recordPassword(principal, newPassword);
	}

	@Override
	public void beforeSetPassword(Principal principal, String password) throws ResourceException {
		beforeChangePassword(principal, password, null);
	}

	@Override
	public void afterSetPassword(Principal principal, String password) throws ResourceException {

	}
	
	@Override
	public PasswordPolicyResource resolvePolicy(Principal currentPrincipal) throws ResourceNotFoundException {
		
		/**
		 * We first check for assigned policies as these will override anything a connector might provide.
		 */
		Collection<PasswordPolicyResource> assignedPolicies = getPersonalResources(currentPrincipal);
		if(!assignedPolicies.isEmpty()) {
			PasswordPolicyResource resolvedPolicy = null;
			for(PasswordPolicyResource policy : assignedPolicies) {
				if(resolvedPolicy==null || policy.getPriority() < resolvedPolicy.getPriority()) {
					resolvedPolicy = policy;
				}
			}
			return resolvedPolicy;
		}
		
		/**
		 * Now check the realm for its default policy for the user
		 */
		PasswordPolicyResource realmPolicy = null;
		if(passwordPolicyResolvers.containsKey(currentPrincipal.getRealmModule())) {
			realmPolicy = passwordPolicyResolvers.get(currentPrincipal.getRealmModule()).resolvePrincipalPasswordPolicy(currentPrincipal);
		}
		
		if(realmPolicy==null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.noPolicyAssigned", currentPrincipal.getPrincipalName());
		}
		return realmPolicy;
	}

	@Override
	public void modifyTemplate(AuthenticationState state, FormTemplate template) {
		if(template instanceof ChangePasswordTemplate) {
			template.getInputFields().add(new DivField("logonPasswordPolicyHolder", "${uiPath}/content/injectedPasswordPolicy.html"));
		}
	}

	@Override
	public PasswordPolicyResource resolvePrincipalPasswordPolicy(Principal principal) {
		return repository.getPolicyByDN("LocalUserDefaultPolicy", principal.getRealm());
	}

	@Override
	public PasswordPolicyResource getPolicyByDN(String dn, Realm realm) {
		return repository.getPolicyByDN(dn, realm);
	}

	@Override
	public PasswordPolicyResource getDefaultPasswordPolicy(Realm realm) {
		return getDefaultPolicy(realm, LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY);
	}

	@Override
	public PasswordPolicyResource getDefaultPolicy(Realm realm, String moduleName) {
		return repository.getDefaultPolicyByModule(realm, moduleName);
	}

	@Override
	public Collection<PasswordPolicyResource> getPoliciesByGroup(Principal principal) {
		return Collections.emptyList();
	}
}