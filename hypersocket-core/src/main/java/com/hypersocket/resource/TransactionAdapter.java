package com.hypersocket.resource;

import java.util.Map;

public class TransactionAdapter<T> implements TransactionOperation<T> {

	public TransactionAdapter() {
	}

	@Override
	public void beforeOperation(T resource, Map<String, String> properties) {
		
	}

	@Override
	public void afterOperation(T resource, Map<String, String> properties) {
		
	}

}
