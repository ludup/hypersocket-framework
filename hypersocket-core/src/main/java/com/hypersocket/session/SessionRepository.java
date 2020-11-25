/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepository;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

public interface SessionRepository extends AbstractEntityRepository<Session,String> {

	Session createSession(String remoteAddress, 
			Principal principal, 
			AuthenticationScheme scheme, 
			String userAgent, 
			String userAgentVersion, 
			String os, 
			String osVersion,
			Map<String, String> parameters, int timeout,
			Realm realm);
	
	Session getSessionById(String id);
	
	void updateSession(Session session);

	List<Session> getActiveSessions();

	List<Session> getSystemSessions();

	Long getActiveSessionCount();

	Map<String, Long> getBrowserCount();

	Map<String, Long> getBrowserCount(Date startDate, Date endDate);
	
	Map<String, Long> getBrowserCount(Date startDate, Date endDate, Realm realm);

	Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers);
	
	Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers, Realm realm);

	Long getActiveSessionCount(boolean distinctUsers);
	
	Long getActiveSessionCount(boolean distinctUsers, Realm realm);

	Map<String, Long> getOSCount(Date startDate, Date endDate);
	
	Map<String, Long> getOSCount(Date startDate, Date endDate, Realm realm);

	Map<String, Long> getIPCount(Date startDate, Date endDate);

	List<Session> search(Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs);

	long getResourceCount(Realm realm, String searchPattern, CriteriaConfiguration... configs);

	Map<String, Long> getPrincipalUsage(Realm realm, int maximumUsers, Date startDate, Date endDate);

	void deleteRealm(Realm realm);

	void cleanUp(Date maxDate);
}
