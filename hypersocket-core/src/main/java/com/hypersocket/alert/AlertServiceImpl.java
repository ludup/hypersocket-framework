package com.hypersocket.alert;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.cache.Cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.cache.CacheService;
import com.hypersocket.events.CoreStartedEvent;
import com.hypersocket.events.EventDefinition;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
import com.hypersocket.tasks.alert.AlertTask;
import com.hypersocket.tasks.alert.AlertTaskRepository;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

@Service
public class AlertServiceImpl implements AlertService {

	@Autowired
	private AlertKeyRepository repository;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private TriggerResourceService triggerService; 
	
	@Autowired
	private AlertTaskRepository taskRepository;
	
	private Map<String,Object> alertLocks = new HashMap<String,Object>(); 
	
	@Override
	public void registerDynamicEvent(TriggerResource trigger) {
		EventDefinition sourceEvent = eventService.getEventDefinition(trigger.getEvent());

		String resourceKey = "event.alert." + trigger.getId();

		I18N.overrideMessage(Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
						resourceKey, trigger.getName(), trigger.getName()));
		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
						resourceKey + ".warning", taskRepository.getValue(trigger,
								"alert.text"), taskRepository.getValue(trigger,
								"alert.text")));

		EventDefinition def = new EventDefinition(
				TriggerResourceServiceImpl.RESOURCE_BUNDLE, resourceKey, "", null);
		if(sourceEvent != null)
			def.getAttributeNames().addAll(sourceEvent.getAttributeNames());

		eventService.registerEventDefinition(def);
	}
	
	@EventListener
	@Override
	public void onStartup(CoreStartedEvent event) {
		for (TriggerResource trigger : triggerService
				.getTriggersByTask(AlertTask.ACTION_GENERATE_ALERT)) {
			registerDynamicEvent(trigger);
		}
	}
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
