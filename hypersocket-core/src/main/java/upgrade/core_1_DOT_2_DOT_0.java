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

import com.hypersocket.session.Session;
import com.hypersocket.session.SessionRepository;

public class core_1_DOT_2_DOT_0 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_1_DOT_2_DOT_0.class);
	
	@Autowired
	SessionRepository sessionRepository;
	
	
	@Override
	public void run() {

		try {
			
			updateSessionStats();
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}


	private void updateSessionStats() {
		
		if(log.isInfoEnabled()) {
			log.info("Starting session stats update");
		}
		
		for(Session session : sessionRepository.allEntities()) {
			if(session.getSignedOut()!=null) {
				session.setSignedOut(session.getSignedOut());
				sessionRepository.saveEntity(session);
			}
		}
		
		if(log.isInfoEnabled()) {
			log.info("Completed updating session stats");
		}
	}
	

}
