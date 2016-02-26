package com.hypersocket.interfaceState.json;

public class UserInterfaceStateUpdate {

	Long resourceId;
	Long principalId;
	String name;
	String preferences;

	public UserInterfaceStateUpdate() {

	}

	public UserInterfaceStateUpdate(Long resourceId, Long principalId,
			String name, String preferences) {
		this.resourceId = resourceId;
		this.principalId = principalId;
		this.name = name;
		this.preferences = preferences;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public Long getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(Long principalId) {
		this.principalId = principalId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}
}