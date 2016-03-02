package com.hypersocket.interfaceState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name = "interface_state")
public class UserInterfaceState extends RealmResource {

	@Column(name = "preferences", length = 8000)
	String preferences;

	public UserInterfaceState() {

	}

	public UserInterfaceState(String preferences) {
		this.preferences = preferences;
	}

	public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}
}
