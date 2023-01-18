package com.hypersocket.tasks;

public class TaskDefinition {

	String resourceKey;
	String displayMode;

	public TaskDefinition() {
	}

	public TaskDefinition(String resourceKey, String displayMode) {
		this.resourceKey = resourceKey;
		this.displayMode = displayMode;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}

	@Override
	public String toString() {
		return "TaskDefinition [resourceKey=" + resourceKey + ", displayMode=" + displayMode + "]";
	}

}
