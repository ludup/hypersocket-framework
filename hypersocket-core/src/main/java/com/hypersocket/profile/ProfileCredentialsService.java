package com.hypersocket.profile;

import java.util.Collection;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.events.UserDeletedEvent;
import com.hypersocket.session.events.SessionOpenEvent;

public interface ProfileCredentialsService {

	void registerProvider(ProfileCredentialsProvider provider);

	Collection<AuthenticationScheme> filterUserSchemes(Principal principal, Collection<AuthenticationScheme> schemes) throws AccessDeniedException;

	void onUserDeleted(UserDeletedEvent event);

	void createProfile(Principal target) throws AccessDeniedException;

	void updateProfile(Principal target) throws AccessDeniedException;

	void deleteProfile(Principal target) throws AccessDeniedException;

	void onCredentialsUpdated(ProfileCredentialsEvent event);

	void onSessionOpen(SessionOpenEvent event);

	void onBatchChange(ProfileBatchChangeEvent event);

	void updateProfile(Profile profile, Principal target) throws AccessDeniedException;

	Profile generateProfile(Principal target) throws AccessDeniedException;

}
