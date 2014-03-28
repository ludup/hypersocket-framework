/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.AbstractResource;

@Entity
@Table(name="properties", uniqueConstraints = {
	    @UniqueConstraint(columnNames={"resourceKey", "resource"})})
public class DatabaseProperty extends AbstractEntity<Long> implements ResourceProperty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="property_id")
	Long id;

	@Column(nullable=false)
	String resourceKey;
	
	@Column(nullable=true, length=8000 /*SQL server limit */)
	String value;
	
	@ManyToOne(optional=true)
	@JoinColumn(name="resource")
	AbstractResource resource;
	
	@Override
	@XmlElement(name="id")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@JsonIgnore
	public AbstractResource getResource() {
		return resource;
	}

	public void setResource(AbstractResource resource) {
		this.resource = resource;
	}
	
	@Override
	public String getResourceKey() {
		return resourceKey;
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}
}
