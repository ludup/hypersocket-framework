package com.hypersocket.interfaceState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "user_interface_state")
public class UserInterfaceState extends RealmResource {

	@Column(name = "bind_resource_id")
	Long bindResourceId;

	@Column(name = "principal_id")
	Long principalId;

	@Column(name = "preferences")
	String preferences;

	public UserInterfaceState() {

	}

	public UserInterfaceState(Long bindResourceId, Long principalId,
			String preferences) {
		this.bindResourceId = bindResourceId;
		this.principalId = principalId;
		this.preferences = preferences;
	}

	public Long getBindResourceId() {
		return bindResourceId;
	}

	public void setBindResourceId(Long bindResourceId) {
		this.bindResourceId = bindResourceId;
	}

	public Long getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(Long principalId) {
		this.principalId = principalId;
	}

	public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}
}
