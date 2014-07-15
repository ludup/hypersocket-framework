/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.attributes.AttributeRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;

public class core_0_DOT_1_DOT_3 implements Runnable {

	@Autowired
	AttributeRepository attributeRepository;

	@Autowired
	RealmRepository realmRepository;
	
	@Override
	public void run() {

//		AttributeCategory cat = new AttributeCategory();
//		cat.setName("Custom Attributes");
//		cat.setContext("user");
//		cat.setWeight(Integer.MAX_VALUE);
//		
//		attributeRepository.saveCategory(cat);
//		
//		Attribute attr = new Attribute();
//		attr.setCategory(cat);
//		attr.setName("My Workstation");
//		attr.setDescription("Enter the name of your workstation here.");
//		attr.setType(AttributeType.TEXT);
//		attr.setWeight(0);
//		
//		attributeRepository.saveAttribute(attr);
		
		try {
			File conf = new File(System.getProperty("hypersocket.conf", "conf"));
			File userAttributes = new File(conf, "i18n" + File.separator + "UserAttributes.properties");
			if(!userAttributes.exists()) {
				userAttributes.createNewFile();
			}
			Realm realm = realmRepository.getRealmByName("Default");
			if(realm!=null) {
				realm.setSystem(true);
				realmRepository.saveRealm(realm);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
