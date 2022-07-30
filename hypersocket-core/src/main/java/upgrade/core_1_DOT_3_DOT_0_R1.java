/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.RoleType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.SessionService;

public class core_1_DOT_3_DOT_0_R1 implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(core_1_DOT_3_DOT_0_R1.class);

	@Autowired
	private SessionService sessionService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private PermissionService permissionService;

	@Override
	public void run() {

		if (log.isInfoEnabled()) {
			log.info("Creating principal roles");
		}

		sessionService.runAsSystemContext(() -> {
			try {
				for (Realm realm : realmService.allRealms()) {

					try {
						permissionService.updateRole(
								permissionService.getRole(PermissionService.OLD_ROLE_ADMINISTRATOR, realm),
								PermissionService.ROLE_REALM_ADMINISTRATOR, null, null, null, null, false, true, false);
					} catch (ResourceNotFoundException e1) {
					}

					for (Iterator<Principal> userIt = realmService.iterateUsers(realm); userIt.hasNext();) {
						Principal user = userIt.next();
						if (user.isHidden()) {
							continue;
						}
						try {
							permissionService.createRole(user.getDescription(), realm, Arrays.asList(user),
									Collections.<Permission>emptyList(), Collections.<Realm>emptyList(), null, true,
									true, RoleType.USER, false, false, false);
						} catch (ResourceCreationException e) {
							log.error("Could not create principal role", e);
						}
					}
					for (Iterator<Principal> userIt = realmService.iterateGroups(realm); userIt.hasNext();) {
						Principal group = userIt.next();
						try {
							permissionService.createRole(group.getPrincipalName(), realm, Arrays.asList(group),
									Collections.<Permission>emptyList(), null, null, true, true, RoleType.GROUP, false,
									false, false);
						} catch (ResourceCreationException e) {
							log.error("Could not create principal role", e);
						}
					}
				}

			} catch (ResourceException | AccessDeniedException e) {
				log.error("Failed to schedule session reaper job", e);
			}
		});

	}

}
