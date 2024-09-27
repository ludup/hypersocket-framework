package com.hypersocket.session.scope;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

import org.springframework.stereotype.Service;

@Service
public class SessionScopeRegistry {

	private final Map<String, SessionScope> registry = new HashMap<>();
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();
	
	public void register(SessionScope scope) {
		
		Objects.requireNonNull(scope);
		Objects.requireNonNull(scope.getScope());
		Objects.requireNonNull(scope.getSource());
		
		writeLock.lock();
		try {
			registry.put(scope.getScope(), scope);
		} finally {
			writeLock.unlock();
		}
	}
	
	public boolean containsScope(String scope) {
		
		if (Objects.isNull(scope)) {
			return false;
		}
		
		readLock.lock();
		
		try {
			return registry.containsKey(scope);
		} finally {
			readLock.unlock();
		}
	}
	
	public Optional<SessionScope> get(String scope) {
		 
		if (Objects.isNull(scope)) {
			return Optional.empty();
		}
		
		readLock.lock();
		
		try {
			return Optional.ofNullable(registry.get(scope));
		} finally {
			readLock.unlock();
		}
	}
	
	public boolean notContainsScope(String scope) {
		return !containsScope(scope);
	}
}
