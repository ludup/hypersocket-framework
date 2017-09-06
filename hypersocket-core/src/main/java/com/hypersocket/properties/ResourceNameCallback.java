package com.hypersocket.properties;

import com.hypersocket.resource.SimpleResource;

public interface ResourceNameCallback<T extends SimpleResource> {
	String getResourceName(T resource);
}
