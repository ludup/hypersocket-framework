/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.session.SessionService;


public class core_2_DOT_1_DOT_6 implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(core_2_DOT_1_DOT_6.class);

	@Autowired
	private RealmRepository realmRepository;
	
	@Autowired
	private PermissionRepository permissionsRepository;
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	public void run() {
		sessionService.runAsSystemContext(() -> doit());
	}
	
	public void doit() {

		log.info("Upgrading authentications schemes");

		for(Realm realm : realmRepository.allRealms()) {
			
			for(Role role : permissionsRepository.getResources(realm)) {
				role.getPermissionRealms().add(role.getRealm());
				permissionsRepository.saveRole(role);
			}
		}
	}


}
