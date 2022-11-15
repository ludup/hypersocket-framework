package com.hypersocket.profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="profiles_credentials")
public class ProfileCredentials extends AbstractEntity<Long> {

	private static final long serialVersionUID = -5706063672863048659L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="credential_id")
	private Long id;
	
	@ManyToOne
	private Profile profile;
	
	@Column(name="state")
	private ProfileCredentialsState state; 
	
	@Column(name="resource_key")
	private String resourceKey; 
	
	@Transient
	private String iconClass;
	
	@Override
	public Long getId() {
		return id;
	}
	
	@JsonIgnore
	public Profile getProfile() {
		return profile;
	}

	public ProfileCredentialsState getState() {
		return state;
	}

	public void setState(ProfileCredentialsState state) {
		this.state = state;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	
	protected void doHashCodeOnKeys(HashCodeBuilder builder) {
		builder.append(resourceKey);
	}
	
	protected void doEqualsOnKeys(EqualsBuilder builder, Object obj) {
		builder.append(resourceKey, obj);
	}

	public String getIconClass() {
		return iconClass;
	}

	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}
	
}
