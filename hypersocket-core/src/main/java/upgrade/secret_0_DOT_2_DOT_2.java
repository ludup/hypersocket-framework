/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.secret.SecretKeyService;


public class secret_0_DOT_2_DOT_2 implements Runnable {
	
	static Logger log = LoggerFactory.getLogger(secret_0_DOT_2_DOT_2.class);
	
	@Autowired
	SecretKeyService secretKeyService; 
	
	@Override
	public void run() {

		try {
			SecretKeyResource r = secretKeyService.getResourceByName("Test Key");
			secretKeyService.deleteResource(r);
		} catch (Exception e) {
			log.error("Failed to delete Test Key", e);
		}
	}
	
}
