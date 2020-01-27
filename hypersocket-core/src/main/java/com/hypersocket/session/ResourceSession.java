package com.hypersocket.session;

import com.hypersocket.resource.Resource;

public interface ResourceSession<T extends Resource> {

	void close();

	T getResource();
}
