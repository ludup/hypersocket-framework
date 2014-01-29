/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.List;
import java.util.Map;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public abstract class AbstractReadOnlyRealmProvider extends ResourceTemplateRepositoryImpl implements RealmProvider {

	@Override
	public final boolean isReadOnly() {
		return true;
	}

	@Override
	public final Principal createUser(Realm realm, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceCreationException {
		throw new UnsupportedOperationException("Cannot create user because realm is read only!");
	}

	@Override
	public final Principal createUser(Realm realm, String username,
			Map<String, String> properties) throws ResourceCreationException {
		throw new UnsupportedOperationException("Cannot create user because realm is read only!");
	}

	@Override
	public final Principal updateUser(Realm realm, Principal user, String username,
			Map<String, String> properties, List<Principal> principals)
			throws ResourceChangeException {
		throw new UnsupportedOperationException("Cannot update user because realm is read only!");
	}

	@Override
	public final void setPassword(Principal principal, String password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException {
		throw new UnsupportedOperationException("Cannot set password because realm is read only!");
	}

	@Override
	public final void setPassword(Principal principal, char[] password,
			boolean forceChangeAtNextLogon) throws ResourceCreationException {
		throw new UnsupportedOperationException("Cannot set password because realm is read only!");
	}

	@Override
	public final boolean requiresPasswordChange(Principal principal) {
		return false;
	}

	@Override
	public final Principal createGroup(Realm realm, String name,
			List<Principal> principals) throws ResourceCreationException {
		throw new UnsupportedOperationException("Cannot create group because realm is read only!");
	}

	@Override
	public final Principal createGroup(Realm realm, String name)
			throws ResourceCreationException {
		throw new UnsupportedOperationException("Cannot create group because realm is read only!");
	}

	@Override
	public final void deleteGroup(Principal group) throws ResourceChangeException {
		throw new UnsupportedOperationException("Cannot delete group because realm is read only!");

	}

	@Override
	public final Principal updateGroup(Realm realm, Principal group, String name,
			List<Principal> principals) throws ResourceChangeException {
		throw new UnsupportedOperationException("Cannot update group because realm is read only!");
	}

	@Override
	public final void deleteUser(Principal user) throws ResourceChangeException {
		throw new UnsupportedOperationException("Cannot delete user because realm is read only!");
	}

	@Override
	public final void deleteRealm(Realm realm) throws ResourceChangeException {
		throw new UnsupportedOperationException("Cannot create user because remote realm is read only!");
	}
}
