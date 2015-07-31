/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.resource.ResourceCreationException;

public class core_1_DOT_0_DOT_0 implements Runnable {
	
	private static Logger log = LoggerFactory.getLogger(core_1_DOT_0_DOT_0.class);
	@Autowired
	RealmRepository realmRepository;

	@Override
	public void run() {

		try {
			doCoreSetup();
			
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private void doCoreSetup() throws ResourceCreationException {
		
	
		for(Realm realm : realmRepository.allRealms()) {
			
			String uuid = UUID.randomUUID().toString();
			if(log.isInfoEnabled()) {
				log.info("Adding uuid " + uuid + " to realm " + realm.getName());
			}
			realm.setUuid(uuid);
			realmRepository.saveRealm(realm);
		}
	}

}
