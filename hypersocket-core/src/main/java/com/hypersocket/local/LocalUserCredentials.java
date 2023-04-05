/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.local;

import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.hypersocket.auth.PasswordEncryptionType;
import com.hypersocket.realm.PrincipalCredentials;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.utils.HypersocketUtils;

@Entity
@Table(name="local_user_credentials")
public class LocalUserCredentials extends AbstractEntity<Long> implements PrincipalCredentials {

	private static final long serialVersionUID = -3299749715239030009L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@OneToOne
	private LocalUser user;
	
	@Column(name="password", nullable=false)
	private byte[] password;
	
	@Column(name="encoded_password", length=1024)
	private String encodedPassword;
	
	@Column(name="salt", nullable=false)
	private byte[] salt;
	
	@Column(name="encoded_salt")
	private String encodedSalt;
	
	@Column(name="rfc2307")
	private String rfc2307Password;
	
	@Column(name="ntlm")
	private String ntlmPassword;
	
	@Column(name="lm")
	private String lmPassword;
	
	@Column(name="encryption_type", nullable=false)
	private PasswordEncryptionType encryptionType;
	
	@Column(name="password_change_required")
	private boolean passwordChangeRequired;

	public LocalUser getUser() {
		return user;
	}

	public void setUser(LocalUser user) {
		this.user = user;
	}

	public byte[] getPassword() {
		return password;
	}
	
	public byte[] getSalt() {
		return salt;
	}
	
	public PasswordEncryptionType getEncryptionType() {
		return encryptionType;
	}
	
	public void setEncryptionType(PasswordEncryptionType encryptionType) {
		this.encryptionType = encryptionType;
	}
	
	public boolean isPasswordChangeRequired() {
		return passwordChangeRequired;
	}

	public void setPasswordChangeRequired(boolean passwordChangeRequired) {
		this.passwordChangeRequired = passwordChangeRequired;
	}

	@Override
	public Long getId() {
		return id;
	}
	
	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		if(user!=null) {
			builder.append(user.getId()==null ? -1 : user.getId());
		}
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		LocalUserCredentials creds = (LocalUserCredentials) obj;
		builder.append(user==null ? -1 : user.getId(), creds==null ? -1 : creds.getUser().getId());
	}

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
		this.password = HypersocketUtils.getUTF8Bytes(encodedPassword);
	}

	public String getEncodedSalt() {
		return encodedSalt;
	}

	public void setEncodedSalt(String encodedSalt) {
		this.encodedSalt = encodedSalt;
		this.salt =  HypersocketUtils.getUTF8Bytes(encodedSalt);
	}
	
	void setPassword(byte[] password) {
		this.password = password;
	}
	
	void setSalt(byte[] salt) {
		this.salt = salt;
	}

	@Override
	public Optional<String> getEncodedPassword(Encoding encoding) {
		switch(encoding) {
		case NTLM:
			return Optional.ofNullable(ntlmPassword);
		case RFC2307:
			return Optional.ofNullable(rfc2307Password);
		case LM:
			return Optional.ofNullable(lmPassword);
		default:
			return Optional.empty();
		}
	}

	public void setRFC2307Password(String rfc2307Password) {
		this.rfc2307Password = rfc2307Password;
	}

	public void setNTLMPassword(String ntlmPassword) {
		this.ntlmPassword = ntlmPassword;
	}

	public void setLMPassword(String lmPassword) {
		this.lmPassword = lmPassword;
	}
}
