package com.hypersocket.realm;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.ResourceUtils;

@Service
public class UserVariableReplacementServiceImpl implements UserVariableReplacementService {

	static Logger log = LoggerFactory.getLogger(UserVariableReplacementServiceImpl.class);
	
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
	
	Set<UserVariableReplacement> additionalReplacements = new HashSet<UserVariableReplacement>();

	public static Set<String> getDefaultReplacements() {
		return defaultReplacements;
	}
	
	@Override
	public void registerReplacement(UserVariableReplacement replacement) {
		additionalReplacements.add(replacement);
	}
	
	@Override
	public String replaceVariables(Principal source, String value) {
		
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(value);

		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		while (matcher.find()) {
			String attributeName = matcher.group(1);
			String replacement;
			if(hasVariable(source, attributeName)) {
				replacement = getVariableValue(source, attributeName);
			} else {
				log.warn("Failed to find replacement token " + attributeName);
				continue;	
			}
		    builder.append(value.substring(i, matcher.start()));
		    if (replacement == null) {
		        builder.append(matcher.group(0));
		    } else {
		        builder.append(replacement);
		    }
		    i = matcher.end();
		}
		
	    builder.append(value.substring(i, value.length()));
		
		return builder.toString();
	}
	
	protected boolean hasVariable(Principal principal, String name) {

		if (name.equals("password")) {
			return true;
		} 
		RealmProvider provider = realmService.getProviderForRealm(principal
				.getRealm());
		
		boolean has = defaultReplacements.contains(name)
				|| provider.getUserPropertyNames(principal).contains(name)
				|| permissionService.getRolePropertyNames().contains(name);
		
		if(!has) {
			for(UserVariableReplacement replacement : additionalReplacements) {
				if(replacement.getVariableNames(principal).contains(name)) {
					has = true;
					break;
				}
			}
		}
		return has;
	}
	
	@Override
	public Set<String> getVariableNames(Realm realm) {
		
		RealmProvider provider = realmService.getProviderForRealm(realm);
		Set<String> names = new TreeSet<String>(defaultReplacements);
		names.addAll(permissionService.getRolePropertyNames());
		names.addAll(provider.getUserPropertyNames(null));
		
		for(UserVariableReplacement replacement : additionalReplacements) {
			names.addAll(replacement.getVariableNames(realm));
		}
		return names;
	}
	
	@Override
	public Set<String> getVariableNames(Principal principal) {
		
		RealmProvider provider = realmService.getProviderForRealm(principal.getRealm());
		Set<String> names = new TreeSet<String>(defaultReplacements);
		names.addAll(permissionService.getRolePropertyNames());
		names.addAll(provider.getUserPropertyNames(principal));
		
		for(UserVariableReplacement replacement : additionalReplacements) {
			names.addAll(replacement.getVariableNames(principal));
		}
		
		return names;
	}

	@Override
	public String getVariableValue(Principal source, String name) {

		if(ResourceUtils.isReplacementVariable(name)) {
			name = ResourceUtils.getReplacementVariableName(name);
		}
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

		for(UserVariableReplacement replacement : additionalReplacements) {
			if(replacement.supportsVariable(source, name)) {
				return replacement.getVariableValue(source, name);
			}
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
