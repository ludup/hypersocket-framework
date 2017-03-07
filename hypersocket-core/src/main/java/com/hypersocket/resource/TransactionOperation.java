package com.hypersocket.resource;

import java.util.Map;

public interface TransactionOperation<T> {

	
	void beforeSetProperties(T resource, Map<String,String> properties) throws ResourceException;
	
	void beforeOperation(T resource, Map<String,String> properties) throws ResourceException;
	
	void afterOperation(T resource, Map<String,String> properties) throws ResourceException;
}
