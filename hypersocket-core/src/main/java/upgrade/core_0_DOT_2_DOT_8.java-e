/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.resource.ResourceCreationException;

public class core_0_DOT_2_DOT_8 implements Runnable {
	
	@Autowired
	RealmRepository realmRepository;

	@Autowired
	LocalUserRepository userRepository;

	
	@Override
	public void run() {

		try {
			doCoreSetup();
			
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private void doCoreSetup() throws ResourceCreationException {
		
		LocalUser user = userRepository.getUserByName("admin", realmRepository.getDefaultRealm());
		user.setType(PrincipalType.USER);
		userRepository.saveUser(user, userRepository.getProperties(user));
	}

}
