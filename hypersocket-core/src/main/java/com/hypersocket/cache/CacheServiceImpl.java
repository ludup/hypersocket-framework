package com.hypersocket.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.cache.ICache;

@Service
public class CacheServiceImpl implements CacheService {

	static Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);
	
	@Autowired
	private CacheManager cacheManager;
	
	private Map<String, CacheRegistration> caches = new LinkedHashMap<>();
	
	@Override
	public <K,V> Cache<K, V> getCacheIfExists(String name, Class<K> key, Class<V> value){
		return cacheManager.getCache(name,key,value);
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	@Override
	public <K, V> Cache<K, V> getCacheOrCreate(String name, Class<K> keyClass, Class<V> valClass) {
		return cache(name, keyClass, valClass, baseConfiguration(keyClass, valClass));
	}

	@Override
	public <K, V> Cache<K, V> getCacheOrCreate(String name, Class<K> keyClass, Class<V> valClass,Factory<? extends ExpiryPolicy> expiryPolicyFactory) {
		return (ICache<K, V>)cache(name, keyClass, valClass, ((MutableConfiguration<K, V>)baseConfiguration(keyClass, valClass)).setExpiryPolicyFactory(expiryPolicyFactory));
	}

	public String getName() {
		return getClass().getName();
	}

	@Override
	public <K, V> V getOrGet(Cache<K, V> cache, K key, Supplier<V> supplier) {
		var el = cache.get(key);
		if(el == null) {
			el = supplier.get();
			if(el != null)
				cache.put(key, el);
		}
		return el;
	}

	private <K,V> CompleteConfiguration<K, V> baseConfiguration(Class<K> key, Class<V> value){
		return new MutableConfiguration<K, V>().setReadThrough(true).setWriteThrough(true).setTypes(key, value).setStatisticsEnabled(true);
	}

	private <K,V> Cache<K, V> cache(String name, Class<K> key, Class<V> value, CompleteConfiguration<K, V> config){
		Cache<K, V> cache = cacheManager.getCache(name,key,value);
		if(cache == null) {
			cache = cacheManager.createCache(name, config);
			caches.put(name, new CacheRegistration(name, key, value, (ICache<?, ?>) cache));
		}
		return cache;
	}

	@Override
	public Map<String, CacheRegistration> getCaches() {
		return caches;
	}

}
