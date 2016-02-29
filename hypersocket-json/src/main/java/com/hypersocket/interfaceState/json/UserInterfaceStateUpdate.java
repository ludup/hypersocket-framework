package com.hypersocket.interfaceState.json;

public class UserInterfaceStateUpdate {

	Long bindResourceId;
	Long resourceId;
	Long principalId;
	String name;
	String preferences;
	boolean specific;

	public UserInterfaceStateUpdate() {

	}

	public UserInterfaceStateUpdate(Long bindResourceId, Long resourceId, Long principalId,
			String name, String preferences, boolean specific) {
		this.bindResourceId = bindResourceId;
		this.resourceId = resourceId;
		this.principalId = principalId;
		this.name = name;
		this.preferences = preferences;
		this.specific = specific;
	}
	
	public Long getBindResourceId() {
		return bindResourceId;
	}

	public void setBindResourceId(Long bindResourceId) {
		this.bindResourceId = bindResourceId;
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

	public boolean isSpecific() {
		return specific;
	}

	public void setSpecific(boolean specific) {
		this.specific = specific;
	}
}