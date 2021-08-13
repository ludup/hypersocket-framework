package com.hypersocket.profile;

import java.util.Collection;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.authenticator.events.AuthenticationSchemeEvent;
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

	Profile createProfile(Principal target) throws AccessDeniedException;

	void updateProfile(Principal target) throws AccessDeniedException;

	void deleteProfile(Principal target) throws AccessDeniedException;

	void onCredentialsUpdated(ProfileCredentialsEvent event);

	void onSessionOpen(SessionOpenEvent event);

	void onBatchChange(ProfileBatchChangeEvent event);

	void updateProfile(Profile profile, Principal target) throws AccessDeniedException;

	Profile generateProfile(Principal target) throws AccessDeniedException;

	Profile updateOrGenerate(Principal target) throws AccessDeniedException;

	Profile getProfileForUser(Principal target) throws AccessDeniedException;

	boolean calculateCompleteness(Profile profile);

	void resetProfile(Principal principal) throws AccessDeniedException, ResourceException;

	void onUserUndeleted(UserUndeletedEvent event);

	void undeleteProfile(Principal target);

	void resumeBatchUpdate(Realm realm);

	void disableBatchUpdate(Realm realm);

	void setValidator(ProfileValidator validator);

}
