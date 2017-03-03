package com.hypersocket.properties;

import com.hypersocket.resource.AbstractResource;

public interface ResourceNameCallback<T extends AbstractResource> {
	String getResourceName(T resource);
}
