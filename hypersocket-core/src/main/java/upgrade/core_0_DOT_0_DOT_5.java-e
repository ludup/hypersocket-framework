/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationModuleRepository;
import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.RealmRepository;

public class core_0_DOT_0_DOT_5 implements Runnable {

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	LocalUserRepository userRepository;

	@Autowired
	PermissionRepository permissionRepository;

	@Autowired
	AuthenticationModuleRepository authenticationRepository;

	@Autowired
	LocalRealmProvider localRealmProvider;

	@Override
	public void run() {

		try {
			
			removePermission(permissionRepository
					.getPermissionByResourceKey("cert.create"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("cert.read"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("cert.update"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("cert.delete"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("permission.realm.admin"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("role.create"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("role.read"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("role.update"));
			removePermission(permissionRepository
					.getPermissionByResourceKey("role.delete"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private void removePermission(Permission perm) {
		
		if(perm==null) {
			return;
		}
		Set<Role> roles = permissionRepository.getRolesWithPermissions(perm);
		
		for(Role role : roles) {
			permissionRepository.revokePermission(role, perm);
		}
		
		permissionRepository.deletePermission(perm);
	}
}
