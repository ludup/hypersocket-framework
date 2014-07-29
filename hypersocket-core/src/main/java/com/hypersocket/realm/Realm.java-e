/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.hypersocket.resource.Resource;

@Entity
@Table(name="realms")
@XmlRootElement(name="realm")
public class Realm extends Resource {
	
	@Column(name="system", nullable=true)
	Boolean system;
	
	public Boolean isSystem() {
		return system!=null && system;
	}
	
	public boolean isSystemResource() {
		return isSystem();
	}

	public void setSystem(Boolean system) {
		this.system = system;
	}
	
	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		builder.append(getName());
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		Realm r = (Realm) obj;
		builder.append(getName(), r.getName());
	}
}
