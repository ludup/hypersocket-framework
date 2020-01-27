/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm.json;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.realm.Realm;

@XmlRootElement(name="roles")
public class RealmList {

	private List<Realm> realms;
	
	@XmlElement(name="realm")
	public List<Realm> getRealms() {
		return realms;
	}
	
	public void setRealms(List<Realm> realms) {
		this.realms = realms;
	}
}
