package com.hypersocket.session;

import com.hypersocket.resource.Resource;

public interface ResourceSession<T extends Resource> {

	public void close();

	public T getResource();
}
