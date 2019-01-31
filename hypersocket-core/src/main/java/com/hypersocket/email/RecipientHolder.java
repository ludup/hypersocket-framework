package com.hypersocket.email;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.realm.Principal;

public class RecipientHolder {

	String name;
	String email;
	Principal principal;
	
	static Set<String> salutations = new HashSet<String>(Arrays.asList("MR", "MS", "MRS", "DR", "PROF"));
	
	public RecipientHolder(String name) {
		
		Pattern depArrHours = Pattern.compile("(?:\"?([^\"]*)\"?\\s)?(?:<?(.+@[^>]+)>?)");
		Matcher matcher = depArrHours.matcher(name);
		matcher.find();
		this.name = matcher.group(1);
		this.email = matcher.group(2);

	}
	
	public RecipientHolder(String name, String email) {
		this.name = name;
		this.email = email;
	}
	
	public RecipientHolder(Principal principal, String email) {
		this.name = principal.getPrincipalDescription();
		this.email = email;
		this.principal = principal;
	}
	
	public RecipientHolder(Principal principal) {
		this.name = principal.getPrincipalDescription();
		this.email = principal.getEmail();
		this.principal = principal;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name==null ? "" : name;
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
	
	public boolean hasPrincipal() {
		return !Objects.isNull(principal);
	}
	
	public Principal getPrincipal() {
		return principal;
	}
	
	public String getPrincipalId() {
		if(principal==null) {
			return "";
		}
		return principal.getId().toString();
	}

	@Override
	public String toString() {
		return String.format("%s <%s>", name, email);
	}
	
	public static void main(String[] srgs) {
		
		new RecipientHolder("Lee Painter <lee@javassh.com>");
		new RecipientHolder("Lee <lee@javassh.com>");
		new RecipientHolder("\"Lee Painter\" <lee@javassh.com>");
		
	}
}
