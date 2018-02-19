package com.hypersocket.permissions;

import java.util.HashSet;
import java.util.Set;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalRepository;

public class RoleUtils {

	public RoleUtils() {
	}

	public static Set<Role> processPermissions(Long[] roles) {
		
		Set<Role> results = new HashSet<Role>();
		
		if(roles!=null) {
			PermissionService permissionService = ApplicationContextServiceImpl.getInstance().getBean(PermissionService.class);
			PrincipalRepository principalRepository = ApplicationContextServiceImpl.getInstance().getBean(PrincipalRepository.class);
			for(Long id : roles) {
				Role r = permissionService.getRoleById(id);
				if(r==null) {
					Principal principal = principalRepository.getResourceById(id);
					if(principal!=null) {
						r = permissionService.getPersonalRole(principal);
					}
				}
				if(r!=null) {
					results.add(r);
				}
			}
		}
		return results;
	}

}
