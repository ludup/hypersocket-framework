/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.menus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.certs.CertificatePermission;
import com.hypersocket.config.ConfigurationPermission;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionStrategy;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.AccessControlPermission;
import com.hypersocket.realm.RealmPermission;

@Service
public class MenuServiceImpl extends AuthenticatedServiceImpl implements
		MenuService {

	static Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

	Map<String, MenuRegistration> rootMenus = new HashMap<String, MenuRegistration>();

	@PostConstruct
	public void postConstruct() {

		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "system", null, 100, null, null,
				null, null));
		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "configuration", "configuration",
				100, ConfigurationPermission.READ, null,
				ConfigurationPermission.UPDATE, null), "system");
		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "certificates", "certificates",
				200, CertificatePermission.CERTIFICATE_ADMINISTRATION, CertificatePermission.CERTIFICATE_ADMINISTRATION,
				CertificatePermission.CERTIFICATE_ADMINISTRATION, CertificatePermission.CERTIFICATE_ADMINISTRATION),
				"system");
		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "shutdown", "shutdown",
				Integer.MAX_VALUE, SystemPermission.SYSTEM_ADMINISTRATION, null,
				SystemPermission.SYSTEM_ADMINISTRATION, null), "system");
		
		
		
		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "security", null, 200, null, null,
				null, null));

		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "realms", "realms", 100,
				RealmPermission.READ, RealmPermission.CREATE,
				RealmPermission.UPDATE, RealmPermission.DELETE), "security");

		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "accessControl", "accessControl",
				200, AccessControlPermission.READ,
				AccessControlPermission.CREATE,
				AccessControlPermission.UPDATE,
				AccessControlPermission.DELETE), "security");

		registerMenu(new MenuRegistration(RESOURCE_BUNDLE, "resources", null, 300, null, null,
				null, null));
	}

	@Override
	public boolean registerMenu(MenuRegistration module) {
		return registerMenu(module, null);
	}

	@Override
	public boolean registerMenu(MenuRegistration module, String parentModule) {

		if (parentModule != null) {
			MenuRegistration parent = rootMenus.get(parentModule);
			if (parent == null) {
				return false;
			}
			parent.addMenu(module);
		} else {
			rootMenus.put(module.getId(), module);
		}

		return true;
	}

	@Override
	public List<Menu> getMenus() {

		List<Menu> userMenus = new ArrayList<Menu>();

		for (MenuRegistration m : rootMenus.values()) {
			try {
				if (m.getReadPermission() != null) {
					assertPermission(m.getReadPermission());
				}
				Menu rootMenu = new Menu(m, 
						hasPermission(m.getCreatePermission()),
						hasPermission(m.getUpdatePermission()),
						hasPermission(m.getDeletePermission()));
				for (MenuRegistration child : m.getMenus()) {
					if (child.getReadPermission() != null) {
						try {
							assertPermission(child.getReadPermission());
						} catch (Exception e) {
							// User does not have access to this menu
							if (log.isDebugEnabled()) {
								log.debug(getCurrentPrincipal().getRealm()
										+ "/" + getCurrentPrincipal().getName()
										+ " does not have access to "
										+ m.getResourceKey()
										+ " menu with permission "
										+ m.getReadPermission());
							}
							continue;
						}
					}

					if (child.hasAccess(getCurrentPrincipal())) {
						rootMenu.getMenus().add(new Menu(child, 
								hasPermission(child.getCreatePermission()),
								hasPermission(child.getUpdatePermission()),
								hasPermission(child.getDeletePermission())));
					}
				}
				if (rootMenu.getResourceName() == null) {
					if (rootMenu.getMenus().size() == 0) {
						if (log.isDebugEnabled()) {
							log.debug("Root menu "
									+ rootMenu.getResourceKey()
									+ " will not be displayed because there are no children and no url has been set");
						}
						continue;
					}
				}
				userMenus.add(rootMenu);
			} catch (AccessDeniedException e) {
				// User does not have access to this menu
				if (log.isDebugEnabled()) {
					log.debug(getCurrentPrincipal().getRealm() + "/"
							+ getCurrentPrincipal().getName()
							+ " does not have access to " + m.getResourceKey()
							+ " menu with permission " + m.getReadPermission());
				}
			}
		}

		Collections.sort(userMenus, new Comparator<Menu>() {
			@Override
			public int compare(Menu o1, Menu o2) {
				return (o1.getWeight() > o2.getWeight() ? 1
						: (o1.getWeight() == o2.getWeight() ? 0 : -1));
			}
		});

		for (Menu m : userMenus) {
			Collections.sort(m.getMenus(), new Comparator<Menu>() {
				@Override
				public int compare(Menu o1, Menu o2) {
					return (o1.getWeight() > o2.getWeight() ? 1 : (o1
							.getWeight() == o2.getWeight() ? 0 : -1));
				}
			});
		}
		return userMenus;
	}

	protected boolean hasPermission(PermissionType permission) {
		try {
			if(permission==null) {
				return false;
			}
			
			verifyPermission(getCurrentPrincipal(),
					PermissionStrategy.REQUIRE_ALL_PERMISSIONS,
					permission);
			return true;
		} catch (AccessDeniedException ex) {
			return false;
		}
	}

}
