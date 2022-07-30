/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.RoleType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.SessionService;


public class core_2_DOT_1_DOT_0 implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(core_2_DOT_1_DOT_0.class);

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private PermissionRepository repository; 
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	public void run() {
		sessionService.runAsSystemContext(() -> doit());
	}
	
	public void doit() {

		log.info("Upgrading roles");
		
		try {
			for(Realm realm : realmService.allRealms()) {
				
				log.info(String.format("Upgrading realm %s", realm.getName()));

				for(Iterator<Principal> userIt = realmService.iterateUsers(realm); userIt.hasNext(); ) {
					Principal principal = userIt.next();
					Role r = permissionService.getPersonalRole(principal);
					r.setType(RoleType.USER);
					repository.saveRole(r);
				}

				for(Iterator<Principal> userIt = realmService.iterateGroups(realm); userIt.hasNext(); ) {
					Principal principal = userIt.next();
					Role r = permissionService.getPersonalRole(principal);
					r.setType(RoleType.GROUP);
					repository.saveRole(r);
				}
				
				for(Role r : permissionService.allRoles(realm)) {
					if(r.getType()==null) {
						if(r.isSystem()) {
							r.setType(RoleType.BUILTIN);
						} else {
							r.setType(RoleType.CUSTOM);
						}
						repository.saveRole(r);
					}
				}
			}
		} catch (AccessDeniedException e) {

		}
		
	}


}
