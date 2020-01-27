/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class AbstractEntity<T> implements Serializable {

	private static final long serialVersionUID = -8808550521563073042L;

	public abstract T getId();
	
	@Column(name="deleted", nullable=false)
	private boolean deleted;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created")
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified")
    private Date modifiedDate = new Date();

	@Column(name="legacy_id")
	private Long legacyId;
	
	@Transient
	private boolean preserveTimestamp = false;
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	@JsonIgnore
	@XmlTransient
	public boolean isDeleted() {
		return deleted;
	}
	
	public Date getCreateDate() {
		return created;
	}
	
	public Date getModifiedDate() {
		return modifiedDate;
	}
	
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public void setCreateDate(Date created) {
		this.created = created;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(31, 17);
		builder.append(getId());
		doHashCodeOnKeys(builder);
		return builder.build();
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		AbstractEntity<T> other = (AbstractEntity<T>) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		
		EqualsBuilder builder = new EqualsBuilder();
		doEqualsOnKeys(builder, obj);
		return builder.build();
	}

	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {

	}

	@Transient
	@JsonIgnore
	public String _meta() {
		return this.getClass().getCanonicalName();
	}

	public Long getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(Long legacyId) {
		this.legacyId = legacyId;
	}

	public void preserveTimestamp() {
		this.preserveTimestamp = true;
	}
	
	boolean isPreserveTimestamp() {
		return preserveTimestamp;
	}

}
