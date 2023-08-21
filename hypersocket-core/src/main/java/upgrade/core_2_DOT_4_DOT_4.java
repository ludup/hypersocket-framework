/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.session.SessionRepository;

public class core_2_DOT_4_DOT_4 implements Runnable {
	private final static Logger LOG = LoggerFactory.getLogger(core_2_DOT_4_DOT_4.class);

	@Autowired
	private SessionRepository repository;

	
	@Override
	public void run() {
		repository.getActiveSessions().forEach(s -> {
			if(s.getSignedOut() == null) {
				LOG.info("Signing out {}", s.getId());
				s.setSignedOut(new Date());
				repository.saveEntity(s);
			}
		});
		
	}

}
