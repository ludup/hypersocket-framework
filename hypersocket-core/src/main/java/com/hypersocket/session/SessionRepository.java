/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.Date;
import java.util.Map;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntityRepository;

public interface SessionRepository extends AbstractEntityRepository<Session,String>, SessionStore {

	Map<String, Long> getBrowserCount();

	Map<String, Long> getBrowserCount(Date startDate, Date endDate);
	
	Map<String, Long> getBrowserCount(Date startDate, Date endDate, Realm realm);

	Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers);
	
	Long getSessionCount(Date startDate, Date endDate, boolean distinctUsers, Realm realm);

	Map<String, Long> getOSCount(Date startDate, Date endDate);
	
	Map<String, Long> getOSCount(Date startDate, Date endDate, Realm realm);

	Map<String, Long> getIPCount(Date startDate, Date endDate);

	Map<String, Long> getPrincipalUsage(Realm realm, int maximumUsers, Date startDate, Date endDate);

	void cleanUp(Date maxDate);

	void signOutActive();
}
