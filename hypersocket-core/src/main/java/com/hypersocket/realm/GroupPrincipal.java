/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.realm;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class GroupPrincipal<U extends UserPrincipal, G extends GroupPrincipal<U, G>> extends Principal {

	private static final long serialVersionUID = 1L;

	@Override
	public PrincipalType getType() {
		return PrincipalType.GROUP;
	}
	
	public abstract PrincipalStatus getPrincipalStatus();
	
	public String getPrincipalDescription() {
		return getName();
	}
	
	@JsonIgnore
	public abstract Set<U> getUsers();
	
	public String getEmail() {
		return "";
	}
	
	@JsonIgnore
	public abstract Set<G> getGroups();
	
	@JsonIgnore
	public abstract Set<G> getParents();

	@Override
	public Date getExpires() {
		return null;
	}
}
