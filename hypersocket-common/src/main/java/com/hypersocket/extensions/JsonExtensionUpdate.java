package com.hypersocket.extensions;

import com.hypersocket.json.ResourceStatus;

public class JsonExtensionUpdate extends ResourceStatus<ExtensionUpdate> {

	String customer;
	
	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}
}
