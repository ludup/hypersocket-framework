/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.constraint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlRootElement;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@XmlRootElement(name="constraint")
public abstract class Constraint extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="constraint_id")
	Long id;
	
	@Column(name="value")
	String value;
	
	@Column(name="type")
	ConstraintType type;
	
	@Column(name="action")
	ConstraintAction action;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public ConstraintAction getAction() {
		return action;
	}
	
	public void setAction(ConstraintAction action) {
		this.action = action;
	}
	
	public ConstraintType getType() {
		return type;
	}
	
	public void setType(ConstraintType type) {
		this.type = type;
	}
	
}
