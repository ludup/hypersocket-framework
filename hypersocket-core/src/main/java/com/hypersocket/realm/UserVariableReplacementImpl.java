package com.hypersocket.realm;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.permissions.PermissionService;

@Component
public class UserVariableReplacementImpl extends
		AbstractVariableReplacementImpl<Principal> implements
		UserVariableReplacement {

	static Set<String> defaultReplacements;
	static {

		defaultReplacements = new HashSet<String>();
//		defaultReplacements.add("password"); Don't advertise
		defaultReplacements.add("principalName");
		defaultReplacements.add("currentUser.email");
		defaultReplacements.add("currentUser.phone");
	}
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	PermissionService permissionService; 

	public static Set<String> getDefaultReplacements() {
		return defaultReplacements;
	}
	
	@Override
	protected boolean hasVariable(Principal principal, String name) {

		if (name.equals("password")) {
			return true;
		} 
		RealmProvider provider = realmService.getProviderForRealm(principal
				.getRealm());
		
		return defaultReplacements.contains(name)
				|| provider.getUserPropertyNames(principal).contains(name)
				|| permissionService.getRolePropertyNames().contains(name);
	}
	
	@Override
	public Set<String> getVariableNames(Realm realm) {
		
		RealmProvider provider = realmService.getProviderForRealm(realm);
		Set<String> names = new HashSet<String>(defaultReplacements);
		names.addAll(permissionService.getRolePropertyNames());
		names.addAll(provider.getUserPropertyNames(null));
		
		return names;
	}
	
	@Override
	public Set<String> getVariableNames(Principal principal) {
		
		RealmProvider provider = realmService.getProviderForRealm(principal.getRealm());
		Set<String> names = new HashSet<String>(defaultReplacements);
		names.addAll(permissionService.getRolePropertyNames());
		names.addAll(provider.getUserPropertyNames(principal));
		
		return names;
	}

	@Override
	public String getVariableValue(Principal source, String name) {

		if (name.equals("password")) {
			return realmService.getCurrentPassword();
		} 
		
		if (defaultReplacements.contains(name)) {
			if(name.equals("principalName")) {
				return source.getPrincipalName();
			} else if(name.equals("currentUser.email")) {
				try {
					return realmService.getPrincipalAddress(source, MediaType.EMAIL);
				} catch (MediaNotFoundException e) {
					return "";
				}
			} else if(name.equals("currentUser.phone")) {
				try {
					return realmService.getPrincipalAddress(source, MediaType.PHONE);
				} catch (MediaNotFoundException e) {
					return "";
				}
			}
			throw new IllegalStateException(
					"We should not be able to reach here. Did you add default replacement without implementing it?");
		}

		if(permissionService.getRolePropertyNames().contains(name)) {
			return permissionService.getRoleProperty(permissionService.getCurrentRole(), name);
		} else {
			RealmProvider provider = realmService.getProviderForRealm(source
					.getRealm());
			return provider.getUserPropertyValue(source, name);
		
		}

	}

}
