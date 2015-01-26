/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.session.Session;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="resource_type" , discriminatorType=DiscriminatorType.STRING)
@Table(name="assignable_resource_sessions")
public abstract class AssignableResourceSession<T extends AssignableResource> extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="id")
	Long id;
	
	@OneToOne
	@JoinColumn(name="session_id")
	Session session;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date opened;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date closed;
	
	@Column(name="total_bytes_out")
	long totalBytesIn;
	
	@Column(name="total_bytes_in")
	long totalBytesOut;
	
	@Override
	public Long getId() {
		return id;
	}

	public Date getOpened() {
		return opened;
	}

	public void setOpened(Date opened) {
		this.opened = opened;
	}

	public abstract T getResource();

	public abstract void setResource(T resource);

	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public Date getClosed() {
		return closed;
	}

	public void setClosed(Date closed) {
		this.closed = closed;
	}

	public long getTotalBytesIn() {
		return totalBytesIn;
	}

	public void setTotalBytesIn(long totalBytesIn) {
		this.totalBytesIn = totalBytesIn;
	}

	public long getTotalBytesOut() {
		return totalBytesOut;
	}

	public void setTotalBytesOut(long totalBytesOut) {
		this.totalBytesOut = totalBytesOut;
	}
	
	public boolean isOpen() {
		return opened!=null && !hasClosed();
	}

	public boolean hasClosed() {
		return closed!=null;
	}
	
	
}
