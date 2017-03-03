package com.hypersocket.properties;

import com.hypersocket.resource.AbstractResource;

public interface NameValueImploder<T extends AbstractResource> {

	String getId(T t);
	String getName(T t);
}
