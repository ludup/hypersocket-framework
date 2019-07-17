package com.hypersocket.profile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.hypersocket.repository.AbstractEntity;

@Entity
@Table(name="profiles_credentials")
public class ProfileCredentials extends AbstractEntity<Long> {

	private static final long serialVersionUID = -5706063672863048659L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="credential_id")
	Long id;
	
	@ManyToOne
	Profile profile;
	
	@Column(name="state")
	ProfileCredentialsState state; 
	
	@Column(name="resource_key")
	String resourceKey; 
	
	@Override
	public Long getId() {
		return id;
	}
	
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
}
