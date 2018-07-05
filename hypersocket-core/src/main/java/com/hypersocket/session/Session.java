/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.utils.HypersocketUtils;

@Entity
@Table(name = "sessions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends AbstractEntity<String> {

	private static final long serialVersionUID = -830036435585689895L;

	static Logger log = LoggerFactory.getLogger(Session.class);

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "id")
	String id;

	@Column(name = "ip_address", nullable = false, insertable = true, updatable = false)
	String remoteAddress;

	@Column(name = "signed_out")
	Date signedOut;

	@Transient
	Date lastUpdated;

	@ManyToOne
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "principal_id", insertable = true, updatable = false)
	Principal principal;

	@ManyToOne
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "impersonating_principal_id", insertable = true, updatable = true)
	Principal impersonatedPrincipal;

	@Column(name = "inherit", nullable = true)
	Boolean inheritPermissions;

	@ManyToOne
	@JoinColumn(name = "current_realm_id")
	Realm currentRealm;

	@ManyToOne
	@JoinColumn(name = "realm_id")
	Realm realm;

	@ManyToOne
	@JoinColumn(name = "authentication_scheme")
	AuthenticationScheme scheme;

	@Column(name = "user_agent", nullable = false)
	String userAgent;

	@Column(name = "user_agent_version", nullable = false)
	String userAgentVersion;

	@Column(name = "os", nullable = false)
	String os;

	@Column(name = "os_version", nullable = false)
	String osVersion;

	@Column(name = "timeout", nullable = true)
	Integer sessionTimeout;

	@Column(name = "non_cookie_key")
	String nonCookieKey;

	@Column(name = "state", length = 8000)
	String state;

	@Column(name = "total_seconds")
	Double totalSeconds;

	@Transient
	Map<String, String> stateParameters;

	@Column(name = "system")
	Boolean system;

	@Column(name = "transient")
	Boolean transientSession;

	@Transient
	String csrfToken = null;
	
	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return getPrincipal().getName();
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
		totalSeconds = calculateTotalSeconds();
	}

	public boolean isTransient() {
		return transientSession != null && transientSession;
	}

	public void setTransient(boolean transientSession) {
		this.transientSession = transientSession;
	}

	protected Double calculateTotalSeconds() { 
		return new BigDecimal(
				new BigDecimal((signedOut!=null ? signedOut.getTime() : System.currentTimeMillis())
						- getCreateDate().getTime()).doubleValue()
				/ 1000).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
	Principal getPrincipal() {
		return principal;
	}

	void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public Principal getCurrentPrincipal() {
		if (!isImpersonating()) {
			return getPrincipal();
		} else {
			return getImpersonatedPrincipal();
		}
	}

	public Principal getImpersonatedPrincipal() {
		return impersonatedPrincipal;
	}

	public void setImpersonatedPrincipal(Principal impersonatedPrincipal) {
		if (this.principal.equals(impersonatedPrincipal)) {
			this.impersonatedPrincipal = null;
		} else {
			this.impersonatedPrincipal = impersonatedPrincipal;
		}
	}

	public boolean isInheritPermissions() {
		return inheritPermissions == null ? false : inheritPermissions;
	}

	public void setInheritPermissions(Boolean inheritPermissions) {
		this.inheritPermissions = inheritPermissions == null ? false : inheritPermissions;
	}

	public Realm getCurrentRealm() {
		if (currentRealm == null) {
			return realm;
		} else {
			return currentRealm;
		}
	}

	public void setCurrentRealm(Realm currentRealm) {
		this.currentRealm = currentRealm;
	}

	public void touch() {
		lastUpdated = new Date();
		
		if (isReadyForUpdate()) {
			ApplicationContextServiceImpl.getInstance().getBean(SessionRepository.class).updateSession(this);
			if (log.isDebugEnabled()) {
				log.debug("Session "
						+ getPrincipal().getPrincipalName()
						+ "/" + getId()
						+ " state has been updated");
			}
		}
	}

	public Date getLastUpdated() {
		if(sessionTimeout != null && sessionTimeout == Integer.MAX_VALUE) {
			return new Date();
		}
		if(lastUpdated != null) {
			return lastUpdated;
		}
		if(getModifiedDate() != null) {
			return getModifiedDate();
		}
		return new Date();
	}

	public void setAuthenticationScheme(AuthenticationScheme scheme) {
		this.scheme = scheme;
	}

	public AuthenticationScheme getAuthenticationScheme() {
		return scheme;
	}

	public int getTimeout() {
		return sessionTimeout == null ? 15 : sessionTimeout;
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
		if(getModifiedDate()==null) {
			return true;
		}
		return System.currentTimeMillis() - getModifiedDate().getTime() > 60000L;
	}

	@JsonIgnore
	public boolean hasLastUpdated() {
		return lastUpdated != null;
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

	public void setStateParameters(Map<String, String> stateParameters) {
		this.stateParameters = stateParameters;
		writeState();
	}

	private void writeState() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream obj = new ObjectOutputStream(out);
			obj.writeObject(stateParameters);
			this.state = HypersocketUtils.base64Encode(out.toByteArray());
		} catch (IOException e) {
			log.error("Could not write session state", e);
		}
	}

	@SuppressWarnings("unchecked")
	public String getStateParameter(String name) {
		if (stateParameters == null) {
			if (state != null) {
				ObjectInputStream obj;
				try {
					obj = new ObjectInputStream(new ByteArrayInputStream(HypersocketUtils.base64Decode(state)));
					stateParameters = (Map<String, String>) obj.readObject();
				} catch (Exception e) {
				}
			}

			if (stateParameters == null) {
				stateParameters = new HashMap<String, String>();
			}
		}
		return stateParameters.get(name);
	}

	public void setStateParameter(String name, String value) {
		if (stateParameters == null) {
			stateParameters = new HashMap<String, String>();
		}
		if(value==null) {
			stateParameters.remove(name);
		} else {
			stateParameters.put(name, value);
		}
		writeState();
	}

	public boolean isImpersonating() {
		return impersonatedPrincipal != null;
	}

	public Principal getInheritedPrincipal() {
		return getPrincipal();
	}
	
	public boolean isClosed() {
		return signedOut!=null;
	}

	public boolean isSystem() {
		return system != null && system;
	}

	public Realm getPrincipalRealm() {
		return realm;
	}

	public void setPrincipalRealm(Realm realm) {
		this.realm = realm;
	}

	public Double getTotalSeconds() {
		if(totalSeconds==null) {
			return calculateTotalSeconds();
		}
		return totalSeconds;
	}

	public Role getCurrentRole() {
		return ApplicationContextServiceImpl.getInstance().getBean(SessionService.class).getCurrentRole(this);
	}
	
	public String getCsrfToken() {
		if(csrfToken==null) {
			csrfToken = DigestUtils.sha256Hex(getId() + "|CSRF_TOKEN");
		}
		return csrfToken;
	}
}
