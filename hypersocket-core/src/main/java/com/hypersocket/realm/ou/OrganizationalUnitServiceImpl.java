package com.hypersocket.realm.ou;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.realm.ProfilePermission;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.UserPermission;

@Service
public class OrganizationalUnitServiceImpl extends AbstractAuthenticatedServiceImpl implements OrganizationalUnitService {

	@Autowired
	private OrganizationalUnitRepository repository;
	
	@Override
	public List<OrganizationalUnit> getOrganizationalUnits() throws AccessDeniedException {
	
		assertAnyPermission(ProfilePermission.READ, UserPermission.READ, UserPermission.UPDATE, UserPermission.CREATE);
		
		return repository.getResources(getCurrentRealm());
	}
	
	@Override
	public List<OrganizationalUnit> getOrganizationalUnits(Realm realm) throws AccessDeniedException {
	
		assertAnyPermission(ProfilePermission.READ, UserPermission.READ, UserPermission.UPDATE, UserPermission.CREATE);
		
		return repository.getResources(realm);
	}
	
	@PostConstruct
	private void setup() {
		EntityResourcePropertyStore.registerResourceService(OrganizationalUnit.class, repository);
	}
}
