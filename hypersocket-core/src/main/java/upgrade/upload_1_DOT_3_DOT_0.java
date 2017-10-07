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

import com.hypersocket.resource.ResourceException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadRepository;
import com.hypersocket.utils.HypersocketUtils;


public class upload_1_DOT_3_DOT_0 implements Runnable {

	static Logger log = LoggerFactory.getLogger(upload_1_DOT_3_DOT_0.class);

	@Autowired
	FileUploadRepository uploadRepository;
	
	@Override
	public void run() {

		for(FileUpload u : uploadRepository.allResources()) {
			String shortCode;
			do {
				shortCode = HypersocketUtils.generateRandomAlphaNumericString(6);
			} while(uploadRepository.getFileByShortCode(shortCode)!=null);
			
			u.setShortCode(shortCode);
			try {
				uploadRepository.saveResource(u);
			} catch (ResourceException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
	}


}
