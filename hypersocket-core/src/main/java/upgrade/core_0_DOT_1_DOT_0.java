/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package upgrade;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.attributes.Attribute;
import com.hypersocket.attributes.AttributeCategory;
import com.hypersocket.attributes.AttributeRepository;
import com.hypersocket.attributes.AttributeType;

public class core_0_DOT_1_DOT_0 implements Runnable {

	@Autowired
	AttributeRepository repostiry;

	@Override
	public void run() {

		AttributeCategory c = new AttributeCategory();
		c.setContext("user");
		c.setName("Contact");
		c.setWeight(100);
		
		repostiry.saveCategory(c);
		
		Attribute a = new Attribute();
		a.setCategory(c);
		a.setName("Mobile Number");
		a.setDescription("Your mobile number, we may use this to send you text.");
		a.setType(AttributeType.TEXT);
		a.setWeight(100);
		
		repostiry.saveAttribute(a);
	
	}
	

}
