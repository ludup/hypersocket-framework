package com.hypersocket.realm.ou;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

public interface OrganizationalUnitService extends AuthenticatedService {

	List<OrganizationalUnit> getOrganizationalUnits()
			throws AccessDeniedException;

	List<OrganizationalUnit> getOrganizationalUnits(Realm realm)
			throws AccessDeniedException;

}
