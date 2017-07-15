package com.hypersocket.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.events.UserCreatedEvent;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.session.events.SessionOpenEvent;

@Service
public class ProfileCredentialsServiceImpl implements ProfileCredentialsService {

	static Logger log = LoggerFactory.getLogger(ProfileCredentialsServiceImpl.class);
	
	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	ProfileRepository profileRepository;
	
	@Autowired
	ClusteredSchedulerService schedulerService; 
	
	Map<String,ProfileCredentialsProvider> providers = new HashMap<String,ProfileCredentialsProvider>();
	
	@Override
	public void registerProvider(ProfileCredentialsProvider provider) {
		providers.put(provider.getResourceKey(), provider);
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
	
	protected Set<ProfileCredentials> collectAuthenticatorStates(Profile profile, Principal principal, Map<String,ProfileCredentials> existingCredentials) throws AccessDeniedException {
		
		Set<ProfileCredentials> states = new HashSet<ProfileCredentials>();
		for(ProfileCredentialsProvider provider : providers.values()) {
			ProfileCredentialsState currentState = provider.hasCredentials(principal);
			if(currentState!=ProfileCredentialsState.NOT_REQUIRED) {
				ProfileCredentials c;
				if(!existingCredentials.containsKey(provider.getResourceKey())) {
					c = new ProfileCredentials();
					c.setResourceKey(provider.getResourceKey());
					c.setProfile(profile);
				} else {
					c = existingCredentials.get(provider.getResourceKey());
				}
				c.setState(currentState);
				states.add(c);
			}
		}
		return states;
	}
	
	protected void calculateCompleteness(Profile profile) {
		
		int complete = 0;
		int partial = 0;
		int incomplete = 0;
		
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
	
	@Override
	public void createProfile(Principal target) throws AccessDeniedException {
		
		Profile profile = new Profile();
		profile.setId(target);
		profile.setRealm(target.getRealm());
		profile.setCredentials(collectAuthenticatorStates(profile, target, new HashMap<String,ProfileCredentials>()));
		calculateCompleteness(profile);
		profileRepository.saveEntity(profile);
	}
	
	@Override
	public void updateProfile(Principal target) throws AccessDeniedException {
		Profile profile = profileRepository.getEntityById(target.getId());
		if(profile==null) {
			createProfile(target);
		} else {
			Map<String,ProfileCredentials> creds = new HashMap<String,ProfileCredentials>();
			for(ProfileCredentials c : profile.getCredentials()) {
				creds.put(c.getResourceKey(), c);
			}
			Set<ProfileCredentials> currentCreds = collectAuthenticatorStates(profile, target, creds);
			profile.getCredentials().clear();
			profile.getCredentials().addAll(currentCreds);
			calculateCompleteness(profile);
			profileRepository.saveEntity(profile);
		}
	}
	
	@Override
	public void deleteProfile(Principal target) {
		
		Profile profile = profileRepository.getEntityById(target.getId());
		if(profile!=null) {
			profileRepository.deleteEntity(profile);
		}
	}
	
	@EventListener
	@Override
	public void onUserCreated(UserCreatedEvent event) {
		if(event.isSuccess()) {
			fireProfileCreationJob(event.getTargetPrincipal());
		}
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
	public void onUserDeleted(UserDeletedEvent event) {
		if(event.isSuccess()) {
			fireProfileDeletionJob(event.getTargetPrincipal());
		}
	}

	private void fireProfileDeletionJob(Principal targetPrincipal) {
		
		PermissionsAwareJobData data = new PermissionsAwareJobData(targetPrincipal.getRealm(), "profileDeletionJob");
		data.put("targetPrincipalId", targetPrincipal.getId());
		
		try {
			schedulerService.scheduleNow(ProfileDeletionJob.class, UUID.randomUUID().toString(), data);
		} catch (SchedulerException e) {
			log.error("Failed to schedule profile deletion job", e);
		}
	}
	
	@EventListener
	@Override
	public void onSessionOpen(SessionOpenEvent event) {
		if(event.isSuccess()) {
			fireProfileCreationJob(event.getTargetPrincipal());
		}
	}
}
