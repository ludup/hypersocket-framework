package com.hypersocket.properties;

import com.hypersocket.resource.SimpleResource;

public interface NameValueImploder<T extends SimpleResource> {

	String getId(T t);
	String getName(T t);
}
