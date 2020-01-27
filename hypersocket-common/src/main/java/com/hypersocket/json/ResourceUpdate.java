package com.hypersocket.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ResourceUpdate {

	private Long id;
	private String name;
	private PropertyItem[] properties;

	public ResourceUpdate() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public PropertyItem[] getProperties() {
		return properties;
	}

	public void setProperties(PropertyItem[] properties) {
		this.properties = properties;
	}	
	
}
