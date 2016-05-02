/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalRealmProviderImpl;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;

public class core_1_DOT_2_DOT_1 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_1_DOT_2_DOT_1.class);
	
	@Autowired
	LocalUserRepository repository;
	
	@Autowired
	RealmRepository realmRepository;
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		try {
			for(Realm realm : realmRepository.allRealms(LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY)) {
				for(LocalUser user : (Collection<LocalUser>)repository.allUsers(realm)) {
	
					try {
						user.setFullname(repository.getValue(user, "user.fullname"));
						user.setEmail(repository.getValue(user, "user.email"));
						user.setMobile(repository.getValue(user, "user.mobile"));
						repository.saveUser(user, null);
					
					} catch(Throwable t) {
						log.error(String.format("Error updating user %s", user.getPrincipalName()), t);
					}
				}
			}
		} catch(Throwable t) {
			log.error("Failed to process user update", t);
		}
	}


	

}
