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

import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalRepository;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;

public class core_1_DOT_2_DOT_3 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_1_DOT_2_DOT_3.class);
	
	@Autowired
	LocalUserRepository repository;
	
	@Autowired
	RealmRepository realmRepository;
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	PrincipalRepository principalRepository;
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		try {
			
			Principal system = realmService.getSystemPrincipal();
			system.setPrincipalType(PrincipalType.SYSTEM);
			principalRepository.saveResource(system, null);
			
			for(Realm realm : realmRepository.allRealms()) {
				
				RealmProvider provider = realmService.getProviderForRealm(realm);
				
				for(Iterator<Principal> userIt = provider.iterateAllPrincipals(realm, PrincipalType.USER); userIt.hasNext(); ) {
					Principal user = userIt.next();
					user.setPrincipalType(PrincipalType.USER);
					principalRepository.saveResource(user, null);
				}

				for(Iterator<Principal> groupIt = provider.iterateAllPrincipals(realm, PrincipalType.GROUP); groupIt.hasNext(); ) {
					Principal group = groupIt.next();
					group.setPrincipalType(PrincipalType.GROUP);
					principalRepository.saveResource(group, null);
				}

				for(Iterator<Principal> serviceIt = provider.iterateAllPrincipals(realm, PrincipalType.SERVICE); serviceIt.hasNext(); ) {
					Principal service = serviceIt.next();
					service.setPrincipalType(PrincipalType.SERVICE);
					principalRepository.saveResource(service, null);
				}
			}
		} catch(Throwable t) {
			log.error("Failed to process user update", t);
		}
	}


	

}
