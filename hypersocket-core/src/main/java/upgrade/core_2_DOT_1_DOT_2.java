/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalUserCredentials;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.session.SessionService;


public class core_2_DOT_1_DOT_2 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_2_DOT_1_DOT_2.class);

	@Autowired
	private LocalUserRepository userRepository;
	
	@Autowired
	private SessionService sessionService;
	
	@Override
	public void run() {
		sessionService.runAsSystemContext(() -> doit());
	}
	
	public void doit() {

		log.info("Upgrading credentials");
		
		userRepository.flush();
		
		for(LocalUserCredentials creds : userRepository.allCredentials()) {
			
			creds.setEncodedPassword(Base64.encodeBase64String(creds.getPassword()));
			creds.setEncodedSalt(Base64.encodeBase64String(creds.getSalt()));
			
			userRepository.saveCredentials(creds);
			
		}
	}


}
