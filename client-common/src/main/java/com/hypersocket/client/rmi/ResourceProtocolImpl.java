package com.hypersocket.client.rmi;

import java.io.Serializable;

public class ResourceProtocolImpl implements ResourceProtocol, Serializable {

	private static final long serialVersionUID = -3020855022266423615L;

	Long id;
	String name;
	Resource group;
	
	public ResourceProtocolImpl() {
	}

	public ResourceProtocolImpl(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getProtocol() {
		return name;
	}
	
	public void setResource(Resource group) {
		this.group = group;
	}
	
	public Resource getResource() {
		return group;
	}

	
}
