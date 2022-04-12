package com.hypersocket.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.auth.Authenticator;
import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.profile.jobs.ProfileBatchUpdateJob;
import com.hypersocket.profile.jobs.ProfileCreationJob;
import com.hypersocket.profile.jobs.ProfileUpdateJob;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPermission;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.realm.events.UserUndeletedEvent;
import com.hypersocket.realm.events.UserUpdatedEvent;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.session.events.SessionOpenEvent;

@Service
public class ProfileCredentialsServiceImpl extends AbstractAuthenticatedServiceImpl implements ProfileCredentialsService {

	static Logger log = LoggerFactory.getLogger(ProfileCredentialsServiceImpl.class);
	
	public static final String RESOURCE_BUNDLE = "ProfileCredentialsService";
	
	@Autowired
	private I18NService i18nService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ProfileRepository profileRepository;
	
	@Autowired
	private ClusteredSchedulerService schedulerService; 
	
	@Autowired
	private RealmService realmService; 
		
	@Autowired
	private AuthenticationService authenticationService; 
	
	private ProfileValidator validator = null;
	
	private Map<String,ProfileCredentialsProvider> providers = new HashMap<String,ProfileCredentialsProvider>();
	
	Set<Realm> disabledRealms = new HashSet<>();
	
	@PostConstruct
	private void postConstruct() {
		
		i18nService.registerBundle(RESOURCE_BUNDLE);
		
		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException {
				profileRepository.deleteRealm(realm);
			}
		
		});
		
	
	}
	
	@Override
	public void registerProvider(ProfileCredentialsProvider provider) {
		providers.put(provider.getResourceKey(), provider);
	}
	
	@Override
	public void setValidator(ProfileValidator validator) {
		this.validator = validator;
	}
	
	@Override
	public boolean areCredentialsRequired(Principal principal, String module) throws AccessDeniedException  {
		
		Profile profile = getProfileForUser(principal);
		if(Objects.isNull(profile)) {
			profile = generateProfile(principal);
		}
		
		for(ProfileCredentials creds : profile.getCredentials()) {
			if(creds.getResourceKey().equals(module)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Collection<AuthenticationScheme> filterUserSchemes(Principal principal, Collection<AuthenticationScheme> schemes) throws AccessDeniedException {
		
		List<AuthenticationScheme> userSchemes = new ArrayList<AuthenticationScheme>();
		for(AuthenticationScheme scheme : schemes) {
			if(!scheme.getAllowedRoles().isEmpty()) {
				if(permissionService.hasRole(principal, scheme.getAllowedRoles())) {
					userSchemes.add(scheme);
				}
			} else if(!scheme.getDeniedRoles().isEmpty()) {
				if(!permissionService.hasRole(principal, scheme.getAllowedRoles())) {
					userSchemes.add(scheme);
				}
			} else {
				userSchemes.add(scheme);
			}
		}
		return userSchemes;
	}
	
	protected boolean collectAuthenticatorStates(Profile profile, Principal principal, List<ProfileCredentials> states, Map<String,ProfileCredentials> existingCredentials) throws AccessDeniedException {
		
		boolean is2FA = false;
		if(Objects.nonNull(validator)) {
			Set<String> nonSelectiveCredentials = new HashSet<>(validator.getRequiredUserCredentials(principal));
			is2FA = nonSelectiveCredentials.contains("2faAuthenticationFlow");
			
			
			if(is2FA) {
				Set<String> selectiveCredentials = new HashSet<>(validator.getRequired2FACredentials(principal));
				nonSelectiveCredentials.removeAll(selectiveCredentials);
			}
			
			if(!nonSelectiveCredentials.isEmpty()) {
				for(String module : nonSelectiveCredentials) {
					Authenticator authenticator = authenticationService.getAuthenticator(module);
					ProfileCredentialsProvider provider = providers.get(authenticator.getCredentialsResourceKey());
					is2FA |= module.equals("2faAuthenticationFlow");
					if(Objects.nonNull(provider)) {
						ProfileCredentialsState currentState = provider.hasCredentials(principal);
						if(currentState!=ProfileCredentialsState.NOT_REQUIRED) {
							ProfileCredentials c;
							if(!existingCredentials.containsKey(provider.getResourceKey())) {
								c = new ProfileCredentials();
								c.setResourceKey(provider.getResourceKey());
							} else {
								c = existingCredentials.get(provider.getResourceKey());
							}
							c.setState(currentState);
							states.add(c);
						}
					}
				}
			} 
			
			if(is2FA) {
				iterate2FACredentials(principal, states, existingCredentials);
			}
		} else {
			iterateAllCredentials(principal, states, existingCredentials);
		}
		
		profile.setSelective(is2FA);
		return is2FA;
	}
	
	private void iterate2FACredentials(Principal principal, List<ProfileCredentials> states, Map<String,ProfileCredentials> existingCredentials) throws AccessDeniedException {
		
		for(String module : validator.getRequired2FACredentials(principal)) {
			Authenticator authenticator = authenticationService.getAuthenticator(module);
			ProfileCredentialsProvider provider = providers.get(authenticator.getCredentialsResourceKey());
			if(Objects.nonNull(provider)) {
				ProfileCredentialsState currentState = provider.hasCredentials(principal);
				if(currentState!=ProfileCredentialsState.NOT_REQUIRED) {
					ProfileCredentials c;
					if(!existingCredentials.containsKey(provider.getResourceKey())) {
						c = new ProfileCredentials();
						c.setResourceKey(provider.getResourceKey());
					} else {
						c = existingCredentials.get(provider.getResourceKey());
					}
					c.setState(currentState);
					states.add(c);
				}
			}
		}
	}
	
	private void iterateAllCredentials(Principal principal, List<ProfileCredentials> states, Map<String,ProfileCredentials> existingCredentials) throws AccessDeniedException {
		
		for(ProfileCredentialsProvider provider : providers.values()) {
			ProfileCredentialsState currentState = provider.hasCredentials(principal);
			if(currentState!=ProfileCredentialsState.NOT_REQUIRED) {
				ProfileCredentials c;
				if(!existingCredentials.containsKey(provider.getResourceKey())) {
					c = new ProfileCredentials();
					c.setResourceKey(provider.getResourceKey());
				} else {
					c = existingCredentials.get(provider.getResourceKey());
				}
				c.setState(currentState);
				states.add(c);
			}
		}
	}

	@Override
	public boolean calculateCompleteness(Profile profile) {
		
		int complete = 0;
		int partial = 0;
		int incomplete = 0;
		
		ProfileCredentialsState previousState = profile.getState();
		
		for(ProfileCredentials s : profile.getCredentials()) {
			switch(s.getState()) {
			case COMPLETE:
				complete++;
				break;
			case INCOMPLETE:
				incomplete++;
				break;
			case PARTIALLY_COMPLETE:
				partial++;
				break;
			default:
			}
		}
		
		boolean is2FA = profile.getSelective() != null && profile.getSelective();
		
		if(Objects.nonNull(validator)) {
			int threshold = is2FA ? validator.getMaximumCompletedAuths(profile.getRealm()) : profile.getCredentials().size();
			if(complete >= threshold) {
				profile.setState(ProfileCredentialsState.COMPLETE);
			} else if(complete > 0 || partial > 0) {
				profile.setState(ProfileCredentialsState.PARTIALLY_COMPLETE);
			} else {
				profile.setState(ProfileCredentialsState.INCOMPLETE);
			}
		} else {
			if(incomplete > 0) {
				if(partial > 0 || complete > 0) {
					profile.setState(ProfileCredentialsState.PARTIALLY_COMPLETE);
				} else {
					profile.setState(ProfileCredentialsState.INCOMPLETE);
				}
			} else if(partial > 0) {
				profile.setState(ProfileCredentialsState.PARTIALLY_COMPLETE);
			} else {
				profile.setState(ProfileCredentialsState.COMPLETE);
			}
		}
		
		switch(profile.getState()) {
		case INCOMPLETE:
		case PARTIALLY_COMPLETE:
			return !Boolean.getBoolean("hypersocket.disableDefaultProfileUpdate");
		default:
			return profile.getState()!=previousState;
		}
		
	}
	
	@Override
	public Profile createProfile(Principal target) throws AccessDeniedException {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Creating profile for user %s", target.getPrincipalName()));
		}
		
		Profile profile = generateProfile(target);
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Saving profile as %s for user %s", profile.getState(), target.getPrincipalName()));
		}
		
		profileRepository.saveEntity(profile);
		return profile;
	}
	
	@Override
	public Profile generateProfile(Principal target) throws AccessDeniedException {
		Profile profile = new Profile();
		profile.setId(target);
		profile.setRealm(target.getRealm());
		
		List<ProfileCredentials> creds = new ArrayList<>();
		collectAuthenticatorStates(profile, target,creds,new HashMap<String,ProfileCredentials>());
		profile.setCredentials(creds);
		calculateCompleteness(profile);
		return profile;
	}
	
	@Override
	public Profile updateOrGenerate(Principal target) throws AccessDeniedException {
		Profile profile = profileRepository.getEntityById(target.getId());
		if(profile!=null) {
			updateProfile(profile, target);
			return profile;
		} else {
			return generateProfile(target);
		}
	}
	
	@Override
	public void updateProfile(Principal target) throws AccessDeniedException {
		Profile profile = profileRepository.getEntityById(target.getId());
		if(profile==null) {
			createProfile(target);
		} else {
			updateProfile(profile, target);
		}
	}
	
	@Override
	public void updateProfile(Profile profile, Principal target) throws AccessDeniedException {

		if(log.isInfoEnabled()) {
			log.info(String.format("Updating profile for user %s", target.getPrincipalName()));
		}
		
		Map<String,ProfileCredentials> creds = new HashMap<String,ProfileCredentials>();
		for(ProfileCredentials c : profile.getCredentials()) {
			creds.put(c.getResourceKey(), c);
		}
		
		List<ProfileCredentials> currentCreds = new ArrayList<>();
		collectAuthenticatorStates(profile, target, currentCreds, creds);
		
		profile.getCredentials().clear();
		StringBuffer names = new StringBuffer();
		for(ProfileCredentials c : currentCreds) {
			if(names.length() > 0) {
				names.append(",");
			}
			names.append(c.getResourceKey());
		}
		if(!currentCreds.isEmpty()) {
			profile.getCredentials().addAll(currentCreds);
		}
		
		calculateCompleteness(profile);
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Saving profile as %s for user %s with credentials %s", 
					profile.getState(), target.getPrincipalName(), names.toString()));
		}
		
		profileRepository.saveEntity(profile);
		
	}
	
	@Override
	public void deleteProfile(Principal target) {
		
		Profile profile = profileRepository.getEntityById(target.getId());
		if(profile!=null) {
			profile.setDeleted(true);
			profileRepository.saveEntity(profile);
		}
	}
	
	@Override
	public void undeleteProfile(Principal target) {
		
		Profile profile = profileRepository.getEntityById(target.getId(), true);
		if(profile!=null) {
			profile.setDeleted(false);
			profileRepository.saveEntity(profile);
		}
	}
	
	@Override
	public Profile getProfileForUser(Principal target) {
		return profileRepository.getEntityById(target.getId());
	}
	
	private void fireProfileCreationJob(Principal targetPrincipal) {
		
		Profile profile = profileRepository.getEntityById(targetPrincipal.getId());
		if(profile==null) {
			PermissionsAwareJobData data = new PermissionsAwareJobData(targetPrincipal.getRealm(), "profileCreationJob");
			data.put("targetPrincipalId", targetPrincipal.getId());
			
			try {
				schedulerService.scheduleNow(ProfileCreationJob.class, UUID.randomUUID().toString(), data);
			} catch (SchedulerException e) {
				log.error("Failed to schedule profile creation job", e);
			}
		}
	}

	@EventListener
	@Override
	public void onCredentialsUpdated(ProfileCredentialsEvent event) {
		if(event.isSuccess()) {
			fireProfileUpdateJob(event.getTargetPrincipal());
		}
	}

	@EventListener
	public void onUserUpdated(UserUpdatedEvent event) {
		if(event.isSuccess()) {
			if(event.getAllChangedProperties().containsKey("email") || 
			   event.getAllChangedProperties().containsKey("mobile") || 
			   event.getAllChangedProperties().containsKey("secondaryEmail") || 
			   event.getAllChangedProperties().containsKey("secondaryMobile")) {
				log.info(String.format("Profile for %s needs update due to change in primary or secondary address changes. %s", event.getTargetPrincipal().getName(), event.getAllChangedProperties() ));
				fireProfileUpdateJob(event.getTargetPrincipal());
			}
		}
	}

	private void fireProfileUpdateJob(Principal targetPrincipal) {
		
		PermissionsAwareJobData data = new PermissionsAwareJobData(targetPrincipal.getRealm(), "profileUpdateJob");
		data.put("targetPrincipalId", targetPrincipal.getId());
		
		try {
			schedulerService.scheduleNow(ProfileUpdateJob.class, UUID.randomUUID().toString(), data);
		} catch (SchedulerException e) {
			log.error("Failed to schedule profile update job", e);
		}
	}
	
	@EventListener
	@Override
	public void onConfigurationChange(ConfigurationValueChangedEvent evt) {
		if(evt.isSuccess()) {
			if("2fa.minimumFactors".equals(evt.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY))) {
				fireBatchUpdateJob(evt.getCurrentRealm());
			}
		}
	}
	
	@EventListener
	@Override
	public void onUserDeleted(UserDeletedEvent event) {
		if(event.isSuccess()) {
			deleteProfile(event.getTargetPrincipal());
		}
	}
	
	@EventListener
	@Override
	public void onUserUndeleted(UserUndeletedEvent event) {
		if(event.isSuccess()) {
			undeleteProfile(event.getTargetPrincipal());
		}
	}

	
	@EventListener
	@Override
	public void onSessionOpen(SessionOpenEvent event) {
		if(event.isSuccess()) {
			fireProfileCreationJob(event.getTargetPrincipal());
		}
	}
	
	@EventListener
	@Override
	public void onBatchChange(ProfileBatchChangeEvent event) {
		if(event.isSuccess()) {
			fireBatchUpdateJob(event.getCurrentRealm());
		}
	}

	private void fireBatchUpdateJob(Realm currentRealm) {
		
		if(disabledRealms.contains(currentRealm)) {
			log.warn("Realm {} is currently disable for batch updates", currentRealm.getName());
			return;
		}
		
		PermissionsAwareJobData data = new PermissionsAwareJobData(currentRealm, "profileBatchUpdateJob");
		
		try {
			schedulerService.scheduleNow(ProfileBatchUpdateJob.class, UUID.randomUUID().toString(), data);
		} catch (SchedulerException e) {
			log.error("Failed to schedule profile batch update job", e);
		}
		
	}
	
	@Override
	public void resetProfile(Principal principal) throws AccessDeniedException, ResourceException {
		
		
		assertAnyPermission(UserPermission.DELETE, UserPermission.UPDATE);
		
		Profile profile = getProfileForUser(principal);
		if(Objects.nonNull(profile)) {
			profileRepository.deleteEntity(profile);
		}
		
		for(ProfileCredentialsProvider provider : providers.values()) {
			provider.deleteCredentials(principal);
		}
		
	}

	@Override
	public void resumeBatchUpdate(Realm realm) {
		disabledRealms.remove(realm);
		log.warn("Realm {} is now enabled batch updates", realm.getName());
		fireBatchUpdateJob(realm);		
	}

	@Override
	public void disableBatchUpdate(Realm realm) {
		log.warn("Realm {} is now disabled batch updates", realm.getName());
		disabledRealms.add(realm);
	}
}
