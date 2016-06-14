package com.hypersocket.resource;

public class PropertyChange {

	private String id;
	private String oldValue;
	private String newValue;

	public PropertyChange(String id, String oldValue, String newValue) {
		super();
		this.id = id;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

}
