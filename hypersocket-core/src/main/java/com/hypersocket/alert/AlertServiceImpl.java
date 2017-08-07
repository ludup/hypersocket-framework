package com.hypersocket.alert;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.cache.CacheService;

@Service
public class AlertServiceImpl implements AlertService {

	@Autowired
	private AlertKeyRepository repository;
	
	@Autowired
	private CacheService cacheService;
	
	Map<String,Object> alertLocks = new HashMap<String,Object>();
	
	@Override
	public <T> T processAlert(
			String resourceKey,
			String alertKey, 
			int delay, 
			int threshold, 
			int timeout,
			AlertCallback<T> callback) {
		Object alertLock = null;
		
		synchronized (alertLocks) {
			if(!alertLocks.containsKey(alertKey)) {
				alertLocks.put(alertKey, new Object());
			}			
			alertLock = alertLocks.get(alertKey);
		}

		synchronized(alertLock) {
	
			Cache<String,Long> lastAlertTimestamp = cacheService.getCacheOrCreate("alertTimestampCache", String.class, Long.class);
			if(lastAlertTimestamp.containsKey(alertKey)) {
				long timestamp = lastAlertTimestamp.get(alertKey);
				if((System.currentTimeMillis() - timestamp) < (delay * 1000)) {
					/**
					 * Do not generate alert because we are within the reset delay 
					 * period of the last alert generated.
					 */
					return null;
				} else {
					lastAlertTimestamp.remove(alertKey);
				}
			}
			
			AlertKey ak = new AlertKey();

			ak.setResourceKey(resourceKey);
			ak.setKey(alertKey);
	
			Calendar c = Calendar.getInstance();
			ak.setTriggered(c.getTime());
	
			repository.saveKey(ak);
	
			c.add(Calendar.MINUTE, -timeout);
			long count = repository
					.getKeyCount(resourceKey, alertKey, c.getTime());
	
			if (count >= threshold) {
	
				repository.deleteKeys(resourceKey, alertKey);
				
				synchronized(alertLocks) {
					alertLocks.remove(alertKey);
				}
				
				lastAlertTimestamp.put(alertKey, System.currentTimeMillis());
				return callback.alert();
			}
			
			return null;
		}
	}
}
