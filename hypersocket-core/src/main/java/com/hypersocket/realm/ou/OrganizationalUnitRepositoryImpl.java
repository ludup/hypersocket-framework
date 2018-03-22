package com.hypersocket.realm.ou;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;

@Repository
public class OrganizationalUnitRepositoryImpl extends
		AbstractResourceRepositoryImpl<OrganizationalUnit> implements
		OrganizationalUnitRepository {

	@Override
	@Transactional(readOnly=true)
	public Map<String, OrganizationalUnit> getMappedOrganizationalUnits(
			Realm realm) {
		Map<String, OrganizationalUnit> ous = new HashMap<String, OrganizationalUnit>();
		for (OrganizationalUnit ou : getResources(realm)) {
			ous.put(ou.getDn(), ou);
		}
		return ous;
	}

	@Override
	@Transactional
	public void saveOrganizationalUnit(OrganizationalUnit ou) {
		save(ou);
	}

	@Override

	protected Class<OrganizationalUnit> getResourceClass() {
		return OrganizationalUnit.class;
	}
	
	@Override
	@Transactional
	public void removeAll(Realm realm) {
		if(getCount(OrganizationalUnit.class, new RealmCriteria(realm)) > 0) {
			/**
			 * This breaks in the cloud?!?!
			 */
			for(OrganizationalUnit o : list(OrganizationalUnit.class, new RealmCriteria(realm))) {
				delete(o);
			}
		}
	}
}
