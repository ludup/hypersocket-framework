/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.hypersocket.resource.RealmResource;

@Entity
@XmlRootElement(name = "principal")
public abstract class Principal extends RealmResource {

	@JsonIgnore
	public Realm getRealm() {
		return super.getRealm();
	}

	public abstract PrincipalType getType();

	@XmlElement(name = "principalName")
	public String getPrincipalName() {
		return getName();
	}

	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		builder.append(getRealm());
		builder.append(getName());
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		Principal r = (Principal) obj;
		builder.append(getRealm(), r.getRealm());
		builder.append(getName(), r.getName());
	}
}
