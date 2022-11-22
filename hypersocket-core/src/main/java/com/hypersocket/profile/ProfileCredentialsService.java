package com.hypersocket.profile;

import java.util.Collection;
import java.util.List;

import com.hypersocket.auth.AuthenticationModulesOperationContext;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.realm.events.UserUndeletedEvent;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.events.SessionOpenEvent;

public interface ProfileCredentialsService {

	void registerProvider(ProfileCredentialsProvider provider);

	Collection<AuthenticationScheme> filterUserSchemes(Principal principal, Collection<AuthenticationScheme> schemes) throws AccessDeniedException;

	void onUserDeleted(UserDeletedEvent event);

	Profile createProfile(Principal target, AuthenticationModulesOperationContext ctx) throws AccessDeniedException;

	void updateProfile(Principal target, AuthenticationModulesOperationContext ctx) throws AccessDeniedException;

	void deleteProfile(Principal target) throws AccessDeniedException;

	void onCredentialsUpdated(ProfileCredentialsEvent event);

	void onSessionOpen(SessionOpenEvent event);

	void onBatchChange(ProfileBatchChangeEvent event);

	void updateProfile(Profile profile, Principal target, AuthenticationModulesOperationContext ctx) throws AccessDeniedException;

	Profile generateProfile(Principal target, AuthenticationModulesOperationContext ctx) throws AccessDeniedException;

	Profile updateOrGenerate(Principal target, AuthenticationModulesOperationContext ctx) throws AccessDeniedException;

	Profile getProfileForUser(Principal target) throws AccessDeniedException;

	void resetProfile(Principal principal) throws AccessDeniedException, ResourceException;
	
	void resetProfiles(List<Principal> principals) throws AccessDeniedException, ResourceException;

	void onUserUndeleted(UserUndeletedEvent event);

	void undeleteProfile(Principal target);

	void resumeBatchUpdate(Realm realm);

	void disableBatchUpdate(Realm realm);

	void setValidator(ProfileValidator validator);

	void onConfigurationChange(ConfigurationValueChangedEvent evt);

	boolean calculateCompleteness(Profile profile);

	boolean areCredentialsRequired(Principal principal, String module) throws AccessDeniedException;

}
