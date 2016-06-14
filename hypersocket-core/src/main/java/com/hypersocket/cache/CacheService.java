package com.hypersocket.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

	@Autowired
	private CacheManager cacheManager;
	
	
	public <K,V> Cache<K, V> getCache(String name,Class<K> key, Class<V> value){
		CompleteConfiguration<K, V> config =
			    new MutableConfiguration<K, V>().setReadThrough(true).setWriteThrough(true).setTypes(key, value);
		
		Cache<K, V> cache = cacheManager.getCache(name,key,value);
		return cache == null ? cacheManager.createCache(name, config) : cache;
	}
}
