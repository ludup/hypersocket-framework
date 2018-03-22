package com.hypersocket.events;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationChangedEvent;
import com.hypersocket.config.ConfigurationServiceImpl;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.i18n.Message;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.resource.AssignableResourceEvent;
import com.hypersocket.triggers.TriggerResourceServiceImpl;

@Service
public class EventServiceImpl implements EventService {

	static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	Map<String, EventDefinition> eventDefinitions = new HashMap<String, EventDefinition>();
	Set<String> attributeNames = new TreeSet<String>();
	public static final String RESOURCE_BUNDLE = "EventService";

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	I18NService i18nService;

	ThreadLocal<Boolean> isDelayingEvents = new ThreadLocal<Boolean>();
	ThreadLocal<LinkedList<SystemEvent>> delayedEvents = new ThreadLocal<LinkedList<SystemEvent>>();
	
	List<EventExtender> extenders = new ArrayList<EventExtender>();
	
	@Override
	public void delayEvents(Boolean val) {
		isDelayingEvents.set(val);
		if(delayedEvents.get()!=null) {
			delayedEvents.set(null);
		}
	}
	
	@Override
	public void registerExtender(EventExtender extender) {
		extenders.add(extender);
	}
	
	@Override
	public void publishDelayedEvents() {
		
		try {
			if(delayedEvents.get()==null) {
				return;
			}
			
			LinkedList<SystemEvent> events = delayedEvents.get();
			if(events!=null) {
				synchronized (events) {
					isDelayingEvents.set(false);
					List<SystemEvent> tmp = new LinkedList<SystemEvent>(events);
					
					for(SystemEvent event : tmp) {
						publishEvent(event);
					}
					
					delayedEvents.get().clear();
					delayedEvents.set(null);
				}
			}
		} catch(Throwable t) {
			log.error("Failed to process delayed events", t);
		}
	}
	
	@Override
	public void rollbackDelayedEvents(boolean fireFailedEvents) {
		
		if(log.isDebugEnabled()) {
			log.debug("Rolling back delayed events [" + fireFailedEvents + "]");
		}
		
		isDelayingEvents.set(false);
		if(delayedEvents.get()==null) {
			return;
		}
		LinkedList<SystemEvent> events = delayedEvents.get();
		synchronized (events) {
			
			if(fireFailedEvents) {
				LinkedList<SystemEvent> tmp = new LinkedList<SystemEvent>(events);
				for(SystemEvent event : tmp) {
					if(!event.isSuccess()) {
						publishEvent(event);
					}
				}
			}
			delayedEvents.get().clear();
			delayedEvents.set(null);
		}
	}
	
	protected void delayEvent(SystemEvent event) {
		
		if(log.isDebugEnabled()) {
			log.debug("Delaying event " + event.getResourceKey());
		}
		
		if(delayedEvents.get()==null) {
			delayedEvents.set(new LinkedList<SystemEvent>());
		}
		
		LinkedList<SystemEvent> events = delayedEvents.get();
		synchronized (events) {
			events.addLast(event);
		}
	}
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		
		registerEvent(ConfigurationChangedEvent.class, ConfigurationServiceImpl.RESOURCE_BUNDLE);
		registerEvent(ResourceEvent.class, RESOURCE_BUNDLE);
		registerEvent(AssignableResourceEvent.class, RESOURCE_BUNDLE);
	}

	@Override
	public void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle) {
		registerEvent(eventClass, resourceBundle, null);
	}

	@Override
	public void registerEventDefinition(EventDefinition def) {
		
		if (log.isInfoEnabled()) {
			log.info("Registering event definition " + def.getResourceKey());
		}
		
		eventDefinitions.put(def.getResourceKey(), def);
		attributeNames.addAll(def.getAttributeNames());
	}

	@Override
	public void registerDynamicEvent(String resourceKey, String name, Set<String> attributeNames, 
			String successMessage, String failureMessage, String warningMessage) {
		
		I18N.overrideMessage(Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
						resourceKey, name, name));
		
		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
						resourceKey + ".success", 
						successMessage, 
						successMessage));
	
		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
						resourceKey + ".failure", 
						failureMessage,
						failureMessage));

		I18N.overrideMessage(
				Locale.ENGLISH,
				new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
						resourceKey + ".warning", 
						failureMessage,
						failureMessage));
		
		Set<String> variableNames = new HashSet<String>();
		
		for(String attributeName : attributeNames) {
			String tmp = WordUtils.capitalize(StringUtils.replaceChars(attributeName,  ".-_", "   "));
			String var = resourceKey + "." + StringUtils.replaceChars(attributeName.toLowerCase(),  "-_ ", "...");
			I18N.overrideMessage(
					Locale.ENGLISH,
					new Message(TriggerResourceServiceImpl.RESOURCE_BUNDLE,
							var, 
							tmp,
							tmp));
			variableNames.add(var);
		}
		
		EventDefinition def = new EventDefinition(
				TriggerResourceServiceImpl.RESOURCE_BUNDLE, resourceKey, "", null);

		def.getAttributeNames().addAll(variableNames);
		
		registerEventDefinition(def);
	}
	
	@Override
	public void registerEvent(Class<? extends SystemEvent> eventClass,
			String resourceBundle,
			EventPropertyCollector propertyCollector) {

		if (log.isInfoEnabled()) {
			log.info("Registering event class " + eventClass.getName());
		}
		
		try {
			String resourceKey = (String) eventClass.getField(
					"EVENT_RESOURCE_KEY").get(null);

			String i18nNamespace = "";
			
			try {
				i18nNamespace = (String) eventClass.getField(
					"EVENT_NAMESPACE").get(null);
			} catch(NoSuchFieldException e) { }
			
			if (log.isInfoEnabled()) {
				log.info("Process event with resource key " + resourceKey);
			}

			eventDefinitions.put(resourceKey, new EventDefinition(
					resourceBundle, resourceKey, i18nNamespace, propertyCollector));

			for (Field field : eventClass.getFields()) {
				if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC
						&& (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {

					if (field.getType().equals(String.class)
							&& field.getName().startsWith("ATTR_")) {
						try {
							String attributeName = (String) field.get(null);
							eventDefinitions.get(resourceKey)
									.getAttributeNames().add(attributeName);
							attributeNames.add(attributeName);
							if (log.isDebugEnabled()) {
								log.debug("Added attribute " + attributeName);
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						}
					}
				}
			}
		} catch (Throwable t) {
			throw new IllegalStateException("Failed to register event class "
					+ eventClass.getName(), t);
		} 

	}
	
	@Override
	public void publishEvent(SystemEvent event) {
		
		if(isDelayEvent()) {
			delayEvent(event);
		} else {
			if (!eventDefinitions.containsKey(event.getResourceKey())) {
				if (log.isWarnEnabled()) {
					log.warn("The system is firing an unregistered event "
							+ event.getResourceKey()
							+ ". Register your event with EventService to remove this message.");
				}
			}
	
			if (!event.isSuccess()) {
				log.error(event.getResourceKey() + " failed", event.getException());
			}
			
			eventPublisher.publishEvent(extendEvent(event));
		}
	}

	private SystemEvent extendEvent(SystemEvent event) {
		for(EventExtender e : extenders) {
			e.extendEvent(event);
		}
		return event;
	}
	
	private boolean isDelayEvent() {
		return isDelayingEvents.get()!=null && isDelayingEvents.get();
	}

	@Override
	public EventDefinition getEventDefinition(String resourceKey) {
		return eventDefinitions.get(resourceKey);
	}

	@Override
	public List<EventDefinition> getEvents() {
		return new ArrayList<EventDefinition>(eventDefinitions.values());
	}
	
	@Override
	public Set<String> getAttributeNames() {
		return attributeNames;
	}
}
