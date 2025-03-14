package com.hypersocket.session;

import java.util.Date;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Realm;

public interface ISession {
	
	boolean isDeleted();
	
	Date getCreateDate();
	
	Date getModifiedDate();
	
	Long getLegacyId();

	String getId();

	String getName();

	String getRemoteAddress();

	Date getSignedOut();

	boolean isTransient();

	Long getPrincipalId();

	String getDescription();

	boolean isInheritPermissions();

	Realm getCurrentRealm();

	Date getLastUpdated();

	AuthenticationScheme getAuthenticationScheme();

	int getTimeout();

	long getCurrentTime();

	boolean isReadyForUpdate();

	boolean hasLastUpdated();

	String getUserAgent();

	String getOs();

	String getUserAgentVersion();

	String getOsVersion();

	String getNonCookieKey();

	boolean isImpersonating();

	boolean isClosed();

	boolean isSystem();

	Realm getPrincipalRealm();

	Double getTotalSeconds();

	String getCsrfToken();

}