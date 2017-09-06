package com.hypersocket.password.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.hypersocket.migration.annotation.LookUpKeys;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.SimpleResource;

@Entity
@Table(name="password_history")
@LookUpKeys(propertyNames = {"legacyId"})
public class PasswordHistory extends SimpleResource {

	private static final long serialVersionUID = 8886201399925580320L;

	@OneToOne
	Principal principal;
	
	@Column(name="encoded_password")
	String encodedPassword;

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
	
	public Principal getPrincipal() {
		return principal;
	}
	
	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}
	
	public String getEncodedPassword() {
		return encodedPassword;
	}
	
	@Override
	public String getName() {
		return getId().toString();
	}

}
