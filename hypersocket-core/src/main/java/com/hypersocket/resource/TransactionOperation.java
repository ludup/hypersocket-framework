package com.hypersocket.resource;

import java.util.Map;

public interface TransactionOperation<T> {

	void beforeOperation(T resource, Map<String,String> properties);
	
	void afterOperation(T resource, Map<String,String> properties);
}
