/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.config.SystemConfigurationRepository;
import com.hypersocket.util.DatabaseInformation;


public class core_1_DOT_3_DOT_0_R1 implements Runnable {

	static Logger log = LoggerFactory.getLogger(core_1_DOT_3_DOT_0_R1.class);

	@Autowired
	SystemConfigurationRepository configurationRepository;;
	
	@Autowired
	DatabaseInformation databaseInformation;
	
	@Override
	public void run() {
		if(databaseInformation.isClean()){
			log.info("Tables not found in database, setting id gen value as true");
			configurationRepository.setValue("orm.on.old", "true");
		}else if(!databaseInformation.isClean() && StringUtils.isEmpty(databaseInformation.getOrmOnOld())){
			log.info("Tables found in database, orm on old value is not empty, setting value as false");
			configurationRepository.setValue("orm.on.old", "false");
		}
		
	}

}
