package com.hypersocket.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.ExpiryPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

	@Autowired
	private CacheManager cacheManager;
	
	
	public <K,V> Cache<K, V> getCache(String name,Class<K> key, Class<V> value){
		return cache(name, key, value, baseConfiguration(key, value));
	}
	
	public <K,V> Cache<K, V> getCache(String name,Class<K> key, Class<V> value,Factory<? extends ExpiryPolicy> expiryPolicyFactory){
		return cache(name, key, value, ((MutableConfiguration<K, V>)baseConfiguration(key, value)).setExpiryPolicyFactory(expiryPolicyFactory));
	}
	
	public CacheManager getCacheManager(){
		return this.cacheManager;
	}
	
	private <K,V> CompleteConfiguration<K, V> baseConfiguration(Class<K> key, Class<V> value){
		return new MutableConfiguration<K, V>().setReadThrough(true).setWriteThrough(true).setTypes(key, value);
	} 
	
	private <K,V> Cache<K, V> cache(String name, Class<K> key, Class<V> value, CompleteConfiguration<K, V> config){
		Cache<K, V> cache = cacheManager.getCache(name,key,value);
		return cache == null ? cacheManager.createCache(name, config) : cache;
	}
	
}
