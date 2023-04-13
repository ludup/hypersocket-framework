/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.config.ConfigurationRepository;
import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;

public class core_2_DOT_4_DOT_3 implements Runnable {

	@Autowired
	private RealmRepository realmRepository;

	@Autowired
	private LocalUserRepository userRepository;

	@Autowired
	private LocalRealmProvider localRealmProvider;
	
	@Autowired
	private ConfigurationRepository configurationRepository;
	
	
	@Override
	public void run() {

		try {
			doFakeUserSetup();
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}


	@SuppressWarnings("unchecked")
	private void doFakeUserSetup() throws ResourceException {
		
		// Create the Fake realm
		Realm fakeRealm = new Realm();
		fakeRealm.setName(RealmService.FAKE_REALM_NAME);
		fakeRealm.setResourceCategory("local");
		fakeRealm.setDefaultRealm(false);
		fakeRealm.setHidden(true);
		fakeRealm.setSystem(false);
		fakeRealm.setUuid(UUID.randomUUID().toString());
		
		fakeRealm = realmRepository.saveRealm(fakeRealm, new HashMap<String,String>(), localRealmProvider);
		realmRepository.flush();
		
		configurationRepository.setValue(fakeRealm, "realm.userVisibleProperties",
				ResourceUtils.implodeValues(Arrays.asList("email", "description", "mobile")));
		
		// Create a fake user
		LocalUser fakeUser = new LocalUser();
		fakeUser.setName(RealmService.FAKE_PRINCIPAL_NAME);
		fakeUser.setType(PrincipalType.FAKE);
		fakeUser.setPrincipalType(PrincipalType.FAKE);
		fakeUser.setRealm(fakeRealm);
		fakeUser.setSystem(false);
		fakeUser.setHidden(true);
		
		userRepository.saveUser(fakeUser, new HashMap<String,String>());

		
	}

}
