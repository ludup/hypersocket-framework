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

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.auth.AuthenticationSchemeRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.SessionService;


public class core_2_DOT_1_DOT_5 implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(core_2_DOT_1_DOT_5.class);

	@Autowired
	private RealmRepository realmRepository;
	
	@Autowired
	private AuthenticationSchemeRepository repository;
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	public void run() {
		sessionService.runAsSystemContext(() -> doit());
	}
	
	public void doit() {

		log.info("Upgrading authentications schemes");

		for(Realm realm : realmRepository.allRealms()) {
			
			for(AuthenticationScheme scheme : repository.allSchemes(realm)) {
				
				switch(scheme.getResourceKey()) {
				case "basic":
				case "sso":
					scheme.setSupportsHomeRedirect(true);
					break;
				default:
					scheme.setSupportsHomeRedirect(false);
				}
				
				log.info(String.format("%s in realm %s", scheme.getResourceKey(), realm.getName()));
				
				try {
					repository.saveResource(scheme);
				} catch (ResourceException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			
		}
	}


}
