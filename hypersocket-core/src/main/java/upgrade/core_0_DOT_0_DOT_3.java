/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationRepository;
import com.hypersocket.auth.UsernameAndPasswordAuthenticator;
import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.realm.RealmRepository;

public class core_0_DOT_0_DOT_3 implements Runnable {

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	LocalUserRepository userRepository;

	@Autowired
	PermissionRepository permissionRepository;

	@Autowired
	AuthenticationRepository authenticationRepository;

	@Autowired
	LocalRealmProvider localRealmProvider;

	@Override
	public void run() {

		try {

			List<String> modules = new ArrayList<String>();
			modules.add(UsernameAndPasswordAuthenticator.RESOURCE_KEY);

			authenticationRepository.createScheme("HTTP", modules, "basic", true);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
