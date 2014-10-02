/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name = "sessions")
@JsonIgnoreProperties(ignoreUnknown=true)
public class Session extends AbstractEntity<String> {

	@Id
	@GeneratedValue(generator="uuid")
	@GenericGenerator(name="uuid", strategy="uuid2")
	@Column(name="id")
	@XmlElement(name="id")
	String id;

	@Column(name="ip_address", nullable=false, insertable=true, updatable=false)
	String remoteAddress;

	@Column(name="signed_out")
	Date signedOut;
	
	@Transient
	Date lastUpdated;
	
	@OneToOne
	@JoinColumn(name="principal_id", insertable=true, updatable=false)
	Principal principal;

	@OneToOne
	@JoinColumn(name="realm_id")
	Realm currentRealm;
	
	@OneToOne
	@JoinColumn(name="authentication_scheme")
	AuthenticationScheme scheme;
	
	@Column(name="user_agent", nullable=false)
	String userAgent;
	
	@Column(name="user_agent_version", nullable=false)
	String userAgentVersion;
	
	@Column(name="os", nullable=false)
	String os;
	
	@Column(name="os_version", nullable=false)
	String osVersion;
	
	@Column(name="timeout", nullable=true)
	Integer sessionTimeout;
	
	@Column(name="non_cookie_key")
	String nonCookieKey;
	
	@Override
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	
	public Date getSignedOut() {
		return signedOut;
	}
	
	public void setSignedOut(Date signedOut) {
		this.signedOut = signedOut;
	}
	
	public Principal getPrincipal() {
		return principal;
	}
	
	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}
	
	public Realm getCurrentRealm() {
		if(currentRealm==null) {
			return principal.getRealm();
		} else {
			return currentRealm;
		}
	}

	public void setCurrentRealm(Realm currentRealm) {
		this.currentRealm = currentRealm;
	}

	public void touch() {
		lastUpdated = new Date();
	}

	public Date getLastUpdated() {
		return lastUpdated == null ? getModifiedDate() : lastUpdated;
	}
	
	public void setAuthenticationScheme(AuthenticationScheme scheme) {
		this.scheme = scheme;
	}
	
	public AuthenticationScheme getAuthenticationScheme() {
		return scheme;
	}
	
	public int getTimeout() {
		return sessionTimeout==null ? 15 : sessionTimeout;
	}
	
	public void setTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	@JsonIgnore
	public boolean isReadyForUpdate() {
		// We save our state every minute
		return System.currentTimeMillis() - getModifiedDate().getTime() > 60000L;
	}
	
	@JsonIgnore
	public boolean hasLastUpdated() {
		return lastUpdated!=null;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public void setOs(String os) {
		this.os = os;
	}
	
	public String getOs() {
		return os;
	}

	public String getUserAgentVersion() {
		return userAgentVersion;
	}

	public void setUserAgentVersion(String userAgentVersion) {
		this.userAgentVersion = userAgentVersion;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public void setNonCookieKey(String nonCookieKey) {
		this.nonCookieKey = nonCookieKey;
	}
	
	public String getNonCookieKey() {
		return nonCookieKey;
	}

}
