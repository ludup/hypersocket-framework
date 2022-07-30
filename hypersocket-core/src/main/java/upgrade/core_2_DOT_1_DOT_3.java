/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.permissions.Role;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.SessionService;


public class core_2_DOT_1_DOT_3 implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(core_2_DOT_1_DOT_3.class);

	@Autowired
	private PermissionRepository permissionRepository;
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	public void run() {
		sessionService.runAsSystemContext(() -> doit());
	}
	
	public void doit() {

		log.info("Upgrading personal roles");

		for(Role role : permissionRepository.allRealmsResources()) {
			
			if(role.isPersonalRole()) {
				role.setPrincipalName(role.getPrincipals().iterator().next().getPrincipalName());
				try {
					permissionRepository.saveResource(role);
				} catch (ResourceException e) {
				}
			}
			
		}
	}


}
