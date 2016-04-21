package com.hypersocket.properties;

import com.hypersocket.resource.Resource;

public interface NameValueImploder<T extends Resource> {

	String getId(T t);
	String getName(T t);
}
