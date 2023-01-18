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

	private final String name;
	private final String address;
	private final Principal principal;
	
	static Set<String> salutations = new HashSet<String>(Arrays.asList("MR", "MS", "MRS", "DR", "PROF"));
	
	public final static String EMAIL_PATTERN = "(?:\"?([^\"]*)\"?\\s)?(?:<?(.+@[^>]+)>?)";
	public final static String GENERIC_PATTERN = "(?:\"?([^\"]*)\"?\\s)?(?:<?(.+)>?)";
	
	public static RecipientHolder ofEmailAddressSpec(String addressSpec) {
		return new RecipientHolder(addressSpec, EMAIL_PATTERN, null);
	}
	
	public static RecipientHolder ofGeneric(String addressSpec) {
		return new RecipientHolder(addressSpec, GENERIC_PATTERN, null);
	}
	
	public static RecipientHolder ofName(String name) {
		return new RecipientHolder(name, "");
	}
	
	public static RecipientHolder ofAddress(String address) {
		return new RecipientHolder("", address);
	}
	
	public static RecipientHolder ofNameAndAddress(String name, String address) {
		return new RecipientHolder(name, address);
	}

	@Deprecated
	public RecipientHolder(String name) {
		this(name, EMAIL_PATTERN, null);
	}
	
	protected RecipientHolder(String addressSpec, String pattern, Principal principal) {
		
		Pattern depArrHours = Pattern.compile(pattern);
		Matcher matcher = depArrHours.matcher(addressSpec);
		if(matcher.find()) {
			this.name = StringUtils.defaultString(matcher.group(1));
			this.address = StringUtils.defaultString(matcher.group(2));
		} else {
			this.address = addressSpec;
			this.name = null;
		}	
		this.principal = principal;
	}

	@Deprecated
	public RecipientHolder(String name, String address) {
		this.name = name;
		this.address = address;
		this.principal = null;
	}
	
	public RecipientHolder(Principal principal, String address) {
		this.name = principal.getDescription();
		this.address = address;
		this.principal = principal;
	}
	
	public RecipientHolder(Principal principal) {
		this.name = principal.getDescription();
		this.address = principal.getEmail();
		this.principal = principal;
	}
	
	@Deprecated
	public String getEmail() {
		return address;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getName() {
		return name==null ? "" : name;
	}
	
	public String getFirstName() {
		String name = getName();
		if(StringUtils.isNotBlank(name)) {
			int idx = name.indexOf(',');
			if(idx != -1) {
				return name.substring(idx + 1).trim();
			}
			else {
				idx = name.indexOf(' ');
				if(idx > 0) {
					String firstName = name.substring(0,  idx);
					int idx2 = name.indexOf(' ', idx+1);
					if(salutations.contains(firstName.toUpperCase()) && idx2 > 0) {
						firstName = name.substring(idx+1, idx2);
					}
					return firstName;
				}
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
		return String.format("%s <%s>", name, address);
	}
	
	public static void main(String[] srgs) {
		
		new RecipientHolder("Lee Painter <lee@javassh.com>");
		new RecipientHolder("Lee <lee@javassh.com>");
		new RecipientHolder("\"Lee Painter\" <lee@javassh.com>");
		new RecipientHolder("\"Lee Painter (Testing Account)\" <test@javassh.com>");
		new RecipientHolder("test@javassh.com");
		new RecipientHolder("");
	}
}
