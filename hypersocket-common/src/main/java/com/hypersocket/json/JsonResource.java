package com.hypersocket.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonResource {

	Long id;
	String name;
	JsonResource[] permissions;
	
	public JsonResource[] getPermissions() {
		return permissions;
	}
	public void setPermissions(JsonResource[] permissions) {
		this.permissions = permissions;
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
	
	
}
