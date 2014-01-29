/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationRepository;
import com.hypersocket.auth.UsernameAndPasswordAuthenticator;
import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;

public class core_0_DOT_0_DOT_1 implements Runnable {

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
			
			Realm realm = new Realm();
			realm.setName("System");
			realm.setResourceCategory("local");
			realm.setHidden(true);
			realmRepository.saveRealm(realm, new HashMap<String,String>(), localRealmProvider);

			LocalUser system = new LocalUser();
			system.setName("system");
			system.setType(PrincipalType.SYSTEM);
			system.setRealm(realm);

			userRepository.saveUser(system, new HashMap<String,String>());

			Role systemRole = permissionRepository.createRole("System", realm);
			permissionRepository.grantPermission(systemRole,
					permissionRepository
							.getPermissionByResourceKey(SystemPermission.SYSTEM
									.getResourceKey()));
			systemRole.getPrincipals().add(system);
			systemRole.setHidden(true);
			permissionRepository.saveRole(systemRole);

			List<String> modules = new ArrayList<String>();
			modules.add(UsernameAndPasswordAuthenticator.RESOURCE_KEY);

			authenticationRepository.createScheme("Default", modules, "basic");

			realm = new Realm();
			realm.setName("Default");
			realm.setResourceCategory("local");
			realmRepository.saveRealm(realm, new HashMap<String,String>(), localRealmProvider);

			
			Principal group = localRealmProvider.createGroup(realm,
					"Administrators");

			List<Principal> groups = new ArrayList<Principal>();
			groups.add(group);

			Principal admin = localRealmProvider.createUser(realm, "admin",
					null, groups);

			localRealmProvider.setPassword(admin, "admin", true);

			Role rAdmin = permissionRepository.createRole(
					"System Administrator", null);
			Permission pAdmin = permissionRepository
					.getPermissionByResourceKey(SystemPermission.SYSTEM_ADMINISTRATION
							.getResourceKey());

			permissionRepository.grantPermission(rAdmin, pAdmin);
			rAdmin.getPrincipals().add(admin);
			rAdmin.getPrincipals().add(group);

			permissionRepository.saveRole(rAdmin);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
