package com.hypersocket.cache;

import java.util.Map;
import java.util.function.Supplier;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Factory;
import javax.cache.expiry.ExpiryPolicy;

import com.hazelcast.cache.ICache;

public interface CacheService {
	
	public final static class CacheRegistration {
		private ICache<?,?> cache;
		private Class<?> keyClass;
		private String name;
		private Class<?> valueClass;
		
		CacheRegistration(String name, Class<?> keyClass, Class<?> valueClass, ICache<?,?> cache) {
			super();
			this.name = name;
			this.keyClass = keyClass;
			this.valueClass = valueClass;
			this.cache = cache;
		}

		public ICache<?, ?> getCache() {
			return cache;
		}

		public Class<?> getKeyClass() {
			return keyClass;
		}

		public String getName() {
			return name;
		}

		public Class<?> getValueClass() {
			return valueClass;
		}
	}

	<K, V> Cache<K, V> getCacheOrCreate(String name, Class<K> keyClass, Class<V> valClass);

	<K, V> Cache<K, V> getCacheOrCreate(String name, Class<K> keyClass, Class<V> valClass,
			Factory<? extends ExpiryPolicy> expiryPolicyFactory);

	CacheManager getCacheManager();

	<K, V> V getOrGet(Cache<K, V> cache, K key, Supplier<V> supplier);

	<K, V> Cache<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value);

	Map<String, CacheRegistration> getCaches();

}
