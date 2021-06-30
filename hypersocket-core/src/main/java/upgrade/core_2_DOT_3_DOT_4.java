/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.SessionService;
import com.hypersocket.tables.ColumnSort;


public class core_2_DOT_3_DOT_4 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_2_DOT_3_DOT_4.class);

	@Autowired
	private LocalUserRepository localUserRepository;
	
	@Autowired
	private SessionService sessionService; 
	
	@Autowired
	private RealmService realmService;
	
	@Override
	public void run() {
		sessionService.executeInSystemContext(new Runnable() {
			public void run() {
				doit();
			}
		});
	}
	
	public void doit() {


		try {		
			for(Realm realm : realmService.allRealms()) {
				log.info(String.format("Giving Posix Id to all users in realm %s", realm.getName()));
				Iterator<LocalUser> it = localUserRepository.iterateUsers(realm, new ColumnSort[0]);
				while(it.hasNext()) {
					LocalUser uit = it.next();
					int nid = localUserRepository.getNextPosixId(realm, LocalUser.class);
					log.info(String.format("    %s = %s", uit.getPrincipalName(), nid));
					uit.setPosixId(nid);
					localUserRepository.saveUser(uit, new HashMap<>());
				}
				

				log.info(String.format("Giving Posix Id to all groups in realm %s", realm.getName()));
				Iterator<LocalGroup> git = localUserRepository.iterateGroups(realm, new ColumnSort[0]);
				while(git.hasNext()) {
					LocalGroup guit = git.next();
					int nid = localUserRepository.getNextPosixId(realm, LocalGroup.class);
					guit.setPosixId(nid);
					log.info(String.format("    %s = %s", guit.getPrincipalName(), nid));
					localUserRepository.saveGroup(guit);
				}
			}
		}
		catch(Exception e) {
			throw new IllegalStateException("Failed to give Posix Id to all local users.", e);
		}
	}


}
