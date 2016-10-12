package com.hypersocket.email;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codemonkey.simplejavamail.Recipient;

import com.hypersocket.realm.Principal;

public class RecipientHolder {

	Recipient recipient;
	Principal principal;
	
	static Set<String> salutations = new HashSet<String>(Arrays.asList("MR", "MS", "MRS", "DR", "PROF"));
	public RecipientHolder(Recipient recipient) {
		this.recipient = recipient;
	}
	
	public RecipientHolder(Recipient recipient, Principal principal) {
		this.recipient = recipient;
		this.principal = principal;
	}
	
	public String getEmail() {
		return recipient.getAddress();
	}
	
	public String getName() {
		return recipient.getName()!=null ? recipient.getName() : "";
	}
	
	public String getFirstName() {
		String name = getName();
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
	
	public String getPrincipalId() {
		if(principal==null) {
			return "";
		}
		return principal.getId().toString();
	}
	
}
