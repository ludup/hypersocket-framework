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
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.utils.HypersocketUtils;

@Entity
@Table(name = "sessions")
@JsonIgnoreProperties(ignoreUnknown = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Session extends AbstractEntity<String> implements ISession {

	private static final long serialVersionUID = -830036435585689895L;

	static Logger log = LoggerFactory.getLogger(Session.class);

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "id")
	private String id;

	@Column(name = "ip_address", nullable = false, insertable = true, updatable = false)
	private String remoteAddress;

	@Column(name = "signed_out")
	private Date signedOut;

	@Transient
	private Date lastUpdated;

//	@ManyToOne
//	@Fetch(FetchMode.SELECT)
//	@JoinColumn(name = "principal_id", insertable = true, updatable = false)
//	@OnDelete(action = OnDeleteAction.CASCADE)
//	private Principal principal;

	@Column(name = "principal_name", nullable = false, insertable = true, updatable = false)
	private String principalName;

	@Column(name = "principal_description", nullable = true, insertable = true, updatable = false)
	private String principalDescription;

	@Column(name="principal_id")
	private Long principalId;

//	@ManyToOne
//	@Fetch(FetchMode.SELECT)
//	@JoinColumn(name = "impersonating_principal_id", insertable = true, updatable = true)
//	@OnDelete(action = OnDeleteAction.CASCADE)
//	private Principal impersonatedPrincipal;

	@Column(name = "impersonating_principal_name", nullable = true, insertable = true, updatable = true)
	private String impersonatingPrincipalName;

	@Column(name = "impersonating_principal_description", nullable = true, insertable = true, updatable = true)
	private String impersonatingPrincipalDescription;

	@Column(name="impersonating_principal_id")
	private Long impersonatingPrincipalId;

	@Column(name = "inherit", nullable = true)
	private Boolean inheritPermissions;

	@ManyToOne
	@JoinColumn(name = "current_realm_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Realm currentRealm;

	@ManyToOne
	@JoinColumn(name = "realm_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Realm realm;

	@ManyToOne
	@JoinColumn(name = "authentication_scheme")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private AuthenticationScheme scheme;

	@Column(name = "user_agent", nullable = false)
	private String userAgent;

	@Column(name = "user_agent_version", nullable = false)
	private String userAgentVersion;

	@Column(name = "os", nullable = false)
	private String os;

	@Column(name = "os_version", nullable = false)
	private String osVersion;

	@Column(name = "timeout", nullable = true)
	private Integer sessionTimeout;

	@Column(name = "non_cookie_key")
	private String nonCookieKey;

	@Column(name = "state", length = 8000)
	private String state;

	@Column(name = "total_seconds")
	private Double totalSeconds;

	@Column(name = "system")
	private Boolean system;

	@Column(name = "transient")
	private Boolean transientSession;

	@Transient
	private String csrfToken = null;

	@Transient
	private Map<String, String> stateParameters;
	
	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return principalName;
	}

	@Override
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	@Override
	public Date getSignedOut() {
		return signedOut;
	}

	public void setSignedOut(Date signedOut) {
		this.signedOut = signedOut;
		totalSeconds = calculateTotalSeconds();
	}

	@Override
	public boolean isTransient() {
		return transientSession != null && transientSession;
	}

	public void setTransient(boolean transientSession) {
		this.transientSession = transientSession;
	}

	protected Double calculateTotalSeconds() { 
		long time = signedOut!=null ? signedOut.getTime() : System.currentTimeMillis();
		BigDecimal tmp = new BigDecimal(time	- getCreateDate().getTime());
		tmp = tmp.divide(new BigDecimal(1000));
		return tmp.setScale(0, RoundingMode.HALF_UP).doubleValue();
	}
	
	Principal getPrincipal(RealmService realmService) {
		return realmService.getPrincipalByName(realm, principalName, PrincipalType.ALL_TYPES);
	}

	void setPrincipal(Principal principal) {
		this.principalName = principal.getName();
		this.principalDescription = principal.getDescription();
		this.principalId = principal.getId();
	}

	@Override
	public Long getPrincipalId() {
		return principalId;
	}

	public String getCurrentPrincipalName() {
		if (isImpersonating()) {
			return impersonatingPrincipalName;
		}
		else {
			return principalName;
		}
	}

	public Long getCurrentPrincipalId() {
		if (isImpersonating()) {
			return impersonatingPrincipalId;
		}
		else {
			return principalId;
		}
	}
	
	public String getCurrentPrincipalDescription() {
		if (isImpersonating()) {
			return impersonatingPrincipalDescription;
		}
		else {
			return principalDescription;
		}
	}
	
	@Override
	public String getDescription() {
		return principalDescription;
	}

	public String getImpersonatedPrincipalName() {
		return impersonatingPrincipalName;
	}

	public Long getImpersonatedPrincipalId() {
		return impersonatingPrincipalId;
	}

	public String getImpersonatedPrincipalDescription() {
		return impersonatingPrincipalDescription;
	}

	public Principal getCurrentPrincipal(RealmService realmService) {
		if (isImpersonating()) {
			return getImpersonatedPrincipal(realmService);
		} else {
			return getPrincipal(realmService);
		}
	}

	public Principal getImpersonatedPrincipal(RealmService realmService) {
		if(impersonatingPrincipalId == null)
			return null;
		else
			return realmService.getPrincipalById(impersonatingPrincipalId);
	}

	public void setImpersonatedPrincipal(Principal impersonatedPrincipal) {
		if (impersonatedPrincipal == null) {
			this.impersonatingPrincipalName = null;
			this.impersonatingPrincipalDescription = null;
			this.impersonatingPrincipalId = null;
		} else {
			this.impersonatingPrincipalId = impersonatedPrincipal.getId();
			this.impersonatingPrincipalName = impersonatedPrincipal.getName();
			this.impersonatingPrincipalDescription = impersonatedPrincipal.getDescription();
		}
	}

	@Override
	public boolean isInheritPermissions() {
		return inheritPermissions == null ? false : inheritPermissions;
	}

	public void setInheritPermissions(Boolean inheritPermissions) {
		this.inheritPermissions = inheritPermissions == null ? false : inheritPermissions;
	}

	@Override
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

	public void updated() {
		lastUpdated = new Date();
	}

	@Override
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

	@Override
	public AuthenticationScheme getAuthenticationScheme() {
		return scheme;
	}

	@Override
	public int getTimeout() {
		return sessionTimeout == null ? 15 : sessionTimeout;
	}

	public void setTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}

	@Override
	@JsonIgnore
	public boolean isReadyForUpdate() {
		// We save our state every minute
		if(getModifiedDate()==null) {
			return true;
		}
		return System.currentTimeMillis() - getModifiedDate().getTime() > 60000L;
	}

	@Override
	@JsonIgnore
	public boolean hasLastUpdated() {
		return lastUpdated != null;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String getUserAgent() {
		return userAgent;
	}

	public void setOs(String os) {
		this.os = os;
	}

	@Override
	public String getOs() {
		return os;
	}

	@Override
	public String getUserAgentVersion() {
		return userAgentVersion;
	}

	public void setUserAgentVersion(String userAgentVersion) {
		this.userAgentVersion = userAgentVersion;
	}

	@Override
	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public void setNonCookieKey(String nonCookieKey) {
		this.nonCookieKey = nonCookieKey;
	}

	@Override
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

	@Override
	public boolean isImpersonating() {
		return getImpersonatedPrincipalName() != null;
	}

	public Principal getInheritedPrincipal(RealmService realmService) {
		return getPrincipal(realmService);
	}
	
	@Override
	public boolean isClosed() {
		return signedOut!=null;
	}

	@Override
	public boolean isSystem() {
		return system != null && system;
	}

	@Override
	public Realm getPrincipalRealm() {
		return realm;
	}

	public void setPrincipalRealm(Realm realm) {
		this.realm = realm;
	}

	@Override
	public Double getTotalSeconds() {
//		if(totalSeconds==null) {
			return calculateTotalSeconds();
//		}
//		return totalSeconds;
	}
	
	@Override
	public String getCsrfToken() {
		if(csrfToken==null) {
			csrfToken = DigestUtils.sha256Hex(getId() + "|CSRF_TOKEN");
		}
		return csrfToken;
	}
	
	public PrincipalRef getReference() {
		return new PrincipalRef(realm, getName());
	}

	public void setSystem(boolean system) {
		this.system = system;		
	}
	
	public boolean isPrincipal(Principal principal) {
		return principal == null || principal.getRealm() == null 
			? false 
			: principal.getRealm().equals(realm) && Objects.equals(principal.getName(), principalName);
	}
}
