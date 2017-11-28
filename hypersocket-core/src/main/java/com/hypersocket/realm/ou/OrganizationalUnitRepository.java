package com.hypersocket.realm.ou;

import java.util.Map;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface OrganizationalUnitRepository extends AbstractResourceRepository<OrganizationalUnit> {

	Map<String, OrganizationalUnit> getMappedOrganizationalUnits(Realm realm);

	void saveOrganizationalUnit(OrganizationalUnit ou);

	void removeAll();

}
