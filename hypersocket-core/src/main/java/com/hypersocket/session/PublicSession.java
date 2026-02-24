package com.hypersocket.session;

import java.security.Principal;
import java.util.Date;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;

public class PublicSession implements ISession {
	
	private final Session delegate;
	private final Principal principal;
	private final Principal currentPrincipal;
	private final Principal impersonatedPrincipal;

	public PublicSession(Session delegate, RealmService realmService) {
		this.delegate = delegate;
		this.principal = realmService.getPrincipalById(delegate.getPrincipalId());
		this.impersonatedPrincipal = delegate.isImpersonating() ? realmService.getPrincipalById(delegate.getImpersonatedPrincipalId()) : null;
		this.currentPrincipal = delegate.isImpersonating() ? impersonatedPrincipal : principal; 
	}
	
	@Override
	public boolean isDeleted() {
		return delegate.isDeleted();
	}

	@Override
	public Date getCreateDate() {
		return delegate.getCreateDate();
	}

	@Override
	public Date getModifiedDate() {
		return delegate.getModifiedDate();
	}

	@Override
	public Long getLegacyId() {
		return delegate.getLegacyId();
	}

	public Principal getPrincipal() {
		return principal;
	}
	
	public Principal getCurrentPrincipal() {
		return currentPrincipal;
	}
	
	public Principal getImpersonatedPrincipal() {
		return impersonatedPrincipal;
	}
	
	public Principal getInheritedPrincipal() {
		return principal;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getRemoteAddress() {
		return delegate.getRemoteAddress();
	}

	@Override
	public Date getSignedOut() {
		return delegate.getSignedOut();
	}

	@Override
	public boolean isTransient() {
		return delegate.isTransient();
	}

	@Override
	public Long getPrincipalId() {
		return delegate.getPrincipalId();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public boolean isInheritPermissions() {
		return delegate.isInheritPermissions();
	}

	@Override
	public Realm getCurrentRealm() {
		return delegate.getCurrentRealm();
	}

	@Override
	public Date getLastUpdated() {
		return delegate.getLastUpdated();
	}

	@Override
	public AuthenticationScheme getAuthenticationScheme() {
		return delegate.getAuthenticationScheme();
	}

	@Override
	public int getTimeout() {
		return delegate.getTimeout();
	}

	@Override
	public long getCurrentTime() {
		return delegate.getCurrentTime();
	}

	@Override
	public boolean isReadyForUpdate() {
		return delegate.isReadyForUpdate();
	}

	@Override
	public boolean hasLastUpdated() {
		return delegate.hasLastUpdated();
	}

	@Override
	public String getUserAgent() {
		return delegate.getUserAgent();
	}

	@Override
	public String getOs() {
		return delegate.getOs();
	}

	@Override
	public String getUserAgentVersion() {
		return delegate.getUserAgentVersion();
	}

	@Override
	public String getOsVersion() {
		return delegate.getOsVersion();
	}

	@Override
	public String getNonCookieKey() {
		return delegate.getNonCookieKey();
	}

	@Override
	public boolean isImpersonating() {
		return delegate.isImpersonating();
	}

	@Override
	public boolean isClosed() {
		return delegate.isClosed();
	}

	@Override
	public boolean isSystem() {
		return delegate.isSystem();
	}

	@Override
	public Realm getPrincipalRealm() {
		return delegate.getPrincipalRealm();
	}

	@Override
	public Double getTotalSeconds() {
		return delegate.getTotalSeconds();
	}

	@Override
	public String getCsrfToken() {
		return delegate.getCsrfToken();
	}
}
