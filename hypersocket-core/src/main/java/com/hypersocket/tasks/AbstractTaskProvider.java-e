package com.hypersocket.tasks;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.RealmService;
import com.hypersocket.triggers.TriggerResourceService;

public abstract class AbstractTaskProvider implements TaskProvider {

	static Logger log = LoggerFactory.getLogger(AbstractTaskProvider.class);
	
	@Autowired
	EventService eventService;
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	TriggerResourceService triggerService;
	
	protected String[] processTokenReplacements(String[] values, SystemEvent event) {
		for(int i=0;i<values.length;i++) {
			values[i] = processTokenReplacements(values[i], event);
		}
		return values;
	}
	
	protected String processTokenReplacements(String value, SystemEvent event) {
		
		if(value==null) {
			return null;
		}
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher matcher = pattern.matcher(value);

		StringBuilder builder = new StringBuilder();
		Set<String> defaultAttributes = triggerService.getDefaultVariableNames();
		int i = 0;
		while (matcher.find()) {
			String attributeName = matcher.group(1);
			String replacement;

			if(event.getAttributes().containsKey(attributeName)) {
				replacement = event.getAttribute(attributeName);
			} else if(defaultAttributes.contains(attributeName)) {
				replacement = triggerService.getDefaultVariableValue(attributeName);
			} else {
				log.warn("Failed to find replacement token " + attributeName);
				continue;	
			}
		    builder.append(value.substring(i, matcher.start()));
		    if (replacement == null) {
		        builder.append(matcher.group(0));
		    } else {
		        builder.append(replacement);
		    }
		    i = matcher.end();
			
		}
		
	    builder.append(value.substring(i, value.length()));
		
		return builder.toString();
	}
	
	@Override
	public Collection<PropertyCategory> getPropertyTemplate(Task task) {
		return getRepository().getPropertyCategories(task);
	}

	@Override
	public Collection<PropertyCategory> getProperties(
			Task task) {
		return getRepository().getPropertyCategories(task);
	}
	
	@Override
	public Map<String,String> getTaskProperties(Task task) {
		return getRepository().getProperties(task);
	}
	
	@Override 
	public Collection<String> getPropertyNames(Task task) {
		return getRepository().getPropertyNames(task);
	}
	
	@Override
	public void taskCreated(Task task) {
		
	}
	
	@Override
	public void taskUpdated(Task task) {
		
	}
	
	@Override
	public void taskDeleted(Task task) {
		getRepository().deletePropertiesForResource(task);
	}

	@Override
	public boolean supportsAutomation() {
		return true;
	}
	
	@Override
	public boolean supportsTriggers() {
		return true;
	}
}
