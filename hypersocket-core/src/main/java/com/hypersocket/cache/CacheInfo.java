package com.hypersocket.cache;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cache.ICache;


public class CacheInfo {
	final static Logger LOG = LoggerFactory.getLogger(CacheInfo.class);
	private String destination;
	private long evictions;
	private long gets;
	private float hitPercentage;
	private long misses;
	private long hits;
	private float missPercentage;
	private long puts;
	private long removals;
	private Date creationTime;
	private Date lastAccessTime;
	private Date lastUpdateTime;
	private long entryCount;
	private String id;
	private long size;

	public CacheInfo() {
	}

	CacheInfo(ICache<?, ?> cache) {
		id = cache.getName();
		size = cache.size();
		evictions = cache.getLocalCacheStatistics().getCacheEvictions();
		gets  = cache.getLocalCacheStatistics().getCacheGets();
		hitPercentage = (float)(Math.round((double)cache.getLocalCacheStatistics().getCacheHitPercentage() * 100d) / 100d);
		hits  = cache.getLocalCacheStatistics().getCacheHits();
		misses  = cache.getLocalCacheStatistics().getCacheMisses();
		missPercentage = (float)(Math.round((double)cache.getLocalCacheStatistics().getCacheMissPercentage() * 100d) / 100d);
		puts  = cache.getLocalCacheStatistics().getCachePuts();
		removals  = cache.getLocalCacheStatistics().getCacheRemovals();
		creationTime  = new Date(cache.getLocalCacheStatistics().getCreationTime());
		lastAccessTime  = new Date(cache.getLocalCacheStatistics().getLastAccessTime());
		lastUpdateTime  = new Date(cache.getLocalCacheStatistics().getLastUpdateTime());
		entryCount  = cache.getLocalCacheStatistics().getOwnedEntryCount();
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public long getEvictions() {
		return evictions;
	}

	public void setEvictions(long evictions) {
		this.evictions = evictions;
	}

	public long getGets() {
		return gets;
	}

	public void setGets(long gets) {
		this.gets = gets;
	}

	public float getHitPercentage() {
		return hitPercentage;
	}

	public void setHitPercentage(float hitPercentage) {
		this.hitPercentage = hitPercentage;
	}

	public long getMisses() {
		return misses;
	}

	public void setMisses(long misses) {
		this.misses = misses;
	}

	public long getHits() {
		return hits;
	}

	public void setHits(long hits) {
		this.hits = hits;
	}

	public float getMissPercentage() {
		return missPercentage;
	}

	public void setMissPercentage(float missPercentage) {
		this.missPercentage = missPercentage;
	}

	public long getPuts() {
		return puts;
	}

	public void setPuts(long puts) {
		this.puts = puts;
	}

	public long getRemovals() {
		return removals;
	}

	public void setRemovals(long removals) {
		this.removals = removals;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(Date lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public long getEntryCount() {
		return entryCount;
	}

	public void setEntryCount(long entryCount) {
		this.entryCount = entryCount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean matches(String searchPattern, String searchColumn) {
		if (StringUtils.isBlank(searchPattern))
			return true;
		return matchesString(id, searchPattern);
	}

	private boolean matchesString(String text, String pattern) {
		return StringUtils.isNotBlank(text) && text.toLowerCase().contains(pattern.toLowerCase());
	}
}
