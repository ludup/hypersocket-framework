package com.hypersocket.session;

import java.util.List;
import java.util.Map;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.utils.HypersocketUtils;

public interface SessionStore {

	default Session createSession(String remoteAddress, 
			Principal principal,
			AuthenticationScheme scheme, 
			String userAgent, 
			String userAgentVersion,
			String os,
			String osVersion,
			Map<String, String> parameters,
			int timeout,
			Realm realm) {

		Session session = new Session();
		session.setPrincipal(principal);
		session.setRemoteAddress(remoteAddress);
		session.setUserAgent(HypersocketUtils.checkNull(userAgent, "unknown"));
		session.setUserAgentVersion(HypersocketUtils.checkNull(userAgentVersion, "unknown"));
		session.setOs(HypersocketUtils.checkNull(os, "unknown"));
		session.setOsVersion(HypersocketUtils.checkNull(osVersion, "unknown"));
		session.setAuthenticationScheme(scheme);
		session.setTimeout(timeout);
		session.setPrincipalRealm(realm);
		session.setSystem(false);
		
		if (parameters != null && !parameters.isEmpty()) {
			session.setStateParameters(parameters);
		}
		
		saveSession(session);
		return session;
	}
	
	Session getSessionById(String id);

	List<Session> getActiveSessions();
	
	List<Session> getPrincipalActiveSessions(Principal principal);

	List<Session> getSystemSessions();

	Long getActiveSessionCount();

	Long getActiveSessionCount(boolean distinctUsers);
	
	Long getActiveSessionCount(boolean distinctUsers, Realm realm);

	void deleteRealm(Realm realm);

	void saveSession(Session session);

	List<Session> search(Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchPattern, CriteriaConfiguration... configs);

	void updateRealmSessions(Realm realm);

	void updatePrincipalSessions(Principal realm);
}
