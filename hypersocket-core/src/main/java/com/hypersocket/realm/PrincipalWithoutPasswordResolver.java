package com.hypersocket.realm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.utils.StaticResolver;

public class PrincipalWithoutPasswordResolver extends StaticResolver {

	static Set<String> salutations = new HashSet<String>(Arrays.asList("MR", "MS", "MRS", "DR", "PROF"));
	
	public PrincipalWithoutPasswordResolver(UserPrincipal<?> principal) {
		super();
		addToken("principalId", principal.getPrincipalName());
		addToken("principalName", principal.getPrincipalName());
		addToken("principalDesc", principal.getDescription());
		addToken("principalRealm", principal.getRealm().getName());
		addToken("firstName", getFirstName(principal.getDescription()));
		addToken("email", principal.getEmail());
		addToken("secondaryEmail", ResourceUtils.createDelimitedString(ResourceUtils.explodeCollectionValues(principal.getSecondaryEmail()), "\r\n"));
		addToken("mobile", principal.getMobile());
		addToken("fullName", principal.getDescription());
		addToken("realmName", principal.getRealm().getName());
		addToken("groupNames",
				String.join(",", principal.getGroups().stream().map(g -> g.getName()).collect(Collectors.toList())));
		addToken("groupPrincipalNames", String.join(",",
				principal.getGroups().stream().map(g -> g.getPrincipalName()).collect(Collectors.toList())));
		addToken("groupIds", String.join(",",
				principal.getGroups().stream().map(g -> String.valueOf(g.getId())).collect(Collectors.toList())));
		addToken("groupUUIDs",
				String.join(",", principal.getGroups().stream().map(g -> g.getUUID()).collect(Collectors.toList())));
		addToken("groupDescriptions",
				String.join(",", principal.getGroups().stream().map(g -> g.getUUID()).collect(Collectors.toList())));
	}
	
	public static Set<String> getVariables() {
		return new HashSet<String>(Arrays.asList("principalName", "principalId",
				"principalDesc", "principalRealm", "firstName", "fullName", "email", "secondaryEmail", "mobile", "realmName",
				"groupNames", "groupPrincipalNames", "groupIds", "groupUUIDs", "groupDescriptions"));
	}
	
	public String getFirstName(String name) {
		if(StringUtils.isNotBlank(name)) {
			int idx = name.indexOf(' ');
			if(idx > 0) {
				String firstName = name.substring(0,  idx);
				int idx2 = name.indexOf(' ', idx+1);
				if(salutations.contains(firstName.toUpperCase()) && idx2 > 0) {
					firstName = name.substring(idx+1, idx2);
				}
				return firstName;
			}
			return name;
		}
		return "";
	}
}
