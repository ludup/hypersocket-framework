/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.auth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name="credentials")
public class Credential extends AbstractEntity<Long> {

	@Id
	@GeneratedValue
	Long id;
	
	@Column(name="type")
	CredentialType type;
	
	@Column(name="idx")
	Integer index;
	
	@Column(name="resource_key")
	String resourceKey;
	
	@Override
	public Long getId() {
		return id;
	}
	
	public CredentialType getType() {
		return type;
	}
	
	public void setType(CredentialType type) {
		this.type = type;
	}
	
	public Integer getIndex() {
		return index;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	public void setResourceKey(String name) {
		this.resourceKey = name;
	}
	
}
