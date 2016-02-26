package com.hypersocket.interfaceState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "user_interface_state")
public class UserInterfaceState extends RealmResource {

//	@Column(name = "resourceId")
//	Long resourceId;

	@Column(name = "principalId")
	Long principalId;

	@Column(name = "preferences")
	String preferences;

	public UserInterfaceState() {

	}

	public UserInterfaceState(/*Long resourceId, */Long principalId,
			String preferences) {
//		this.resourceId = resourceId;
		this.principalId = principalId;
		this.preferences = preferences;
	}

//	public Long getResourceId() {
//		return resourceId;
//	}
//
//	public void setResourceId(Long resourceId) {
//		this.resourceId = resourceId;
//	}

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
