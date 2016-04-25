package com.hypersocket.realm;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
				|| provider.getUserPropertyNames(principal).contains(name);
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

		RealmProvider provider = realmService.getProviderForRealm(source
				.getRealm());
		return provider.getUserPropertyValue(source, name);

	}

}
