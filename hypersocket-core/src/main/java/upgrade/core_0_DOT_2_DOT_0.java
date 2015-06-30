/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.attributes.user.UserAttributeRepository;
import com.hypersocket.auth.AuthenticationModuleRepository;
import com.hypersocket.auth.AuthenticationSchemeRepository;
import com.hypersocket.config.ConfigurationRepository;
import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.resource.ResourceCreationException;

public class core_0_DOT_2_DOT_0 implements Runnable {

	@Autowired
	UserAttributeRepository attributeRepository;

	@Autowired
	RealmRepository realmRepository;

	@Autowired
	LocalUserRepository userRepository;

	@Autowired
	PermissionRepository permissionRepository;

	@Autowired
	AuthenticationModuleRepository authenticationRepository;

	@Autowired
	AuthenticationSchemeRepository schemeRepository;
	
	@Autowired
	LocalRealmProvider localRealmProvider;
	
	@Autowired
	ConfigurationRepository configurationRepository;
	
	
	@Override
	public void run() {

		try {
			doCoreSetup();
			
//			doAttributeSetup();
			
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * Keep this commented.
	 */
//	private void doAttributeSetup() throws IOException {
//		
//		AttributeCategory cat = new AttributeCategory();
//		cat.setName("Custom Attributes");
//		cat.setContext("user");
//		cat.setWeight(Integer.MAX_VALUE);
//		
//		attributeRepository.saveCategory(cat);
//		
//		Attribute attr = new Attribute();
//		attr.setCategory(cat);
//		attr.setName("My Workstation");
//		attr.setDescription("Enter the name of your workstation here.");
//		attr.setType(AttributeType.TEXT);
//		attr.setWeight(0);
//		
//		attributeRepository.saveAttribute(attr);
//		
//		File conf = new File(System.getProperty("hypersocket.conf", "conf"));
//		File userAttributes = new File(conf, "i18n" + File.separator + "UserAttributes.properties");
//		if(!userAttributes.exists()) {
//			userAttributes.getParentFile().mkdirs();
//			userAttributes.createNewFile();
//		}
//		Realm realm = realmRepository.getRealmByName("System");
//		if(realm!=null) {
//			realm.setSystem(true);
//			realmRepository.saveRealm(realm);
//		}
//	}

	private void doCoreSetup() throws ResourceCreationException {
		
		// Create the System realm
		Realm realm = new Realm();
		realm.setName("System");
		realm.setResourceCategory("local");
		realm.setDefaultRealm(true);
		realm.setHidden(false);
		realm.setSystem(true);
		
		realmRepository.saveRealm(realm, new HashMap<String,String>(), localRealmProvider);

		configurationRepository.setValue(realm, "realm.userEditableProperties",
				ResourceUtils.implodeValues(Arrays.asList("user.email", "user.fullname", "user.mobile")));
		
		// Create a system user
		LocalUser system = new LocalUser();
		system.setName("system");
		system.setType(PrincipalType.SYSTEM);
		system.setRealm(realm);
		system.setSystem(true);
		system.setHidden(true);
		
		userRepository.saveUser(system, new HashMap<String,String>());

		// Create a system role
		Role systemRole = permissionRepository.createRole("System", realm);
		permissionRepository.grantPermission(systemRole,
				permissionRepository
						.getPermissionByResourceKey(SystemPermission.SYSTEM
								.getResourceKey()));
		systemRole.getPrincipals().add(system);
		systemRole.setHidden(true);
		permissionRepository.saveRole(systemRole);


		// Create Administrators group for Default realm
		Principal group = localRealmProvider.createGroup(realm,
				"Administrators", new HashMap<String,String>());

		List<Principal> groups = new ArrayList<Principal>();
		groups.add(group);

		// Create the default admin user
		Principal admin = localRealmProvider.createUser(realm, "admin",
				null, groups, "admin", true);

		// Create the System Administrator role
		Role rAdmin = permissionRepository.createRole(
				"System Administrator", realm, false, false, true, true);
		Permission pAdmin = permissionRepository
				.getPermissionByResourceKey(SystemPermission.SYSTEM_ADMINISTRATION
						.getResourceKey());

		permissionRepository.grantPermission(rAdmin, pAdmin);
		rAdmin.getPrincipals().add(admin);
		rAdmin.getPrincipals().add(group);

		permissionRepository.saveRole(rAdmin);
	}

}
