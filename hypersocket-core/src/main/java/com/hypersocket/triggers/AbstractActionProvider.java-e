package com.hypersocket.triggers;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventDefinition;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.RealmService;

public abstract class AbstractActionProvider implements TriggerActionProvider {

	static Logger log = LoggerFactory.getLogger(AbstractActionProvider.class);
	
	@Autowired
	EventService eventService;
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	TriggerResourceService triggerService;
	
	protected String processTokenReplacements(String value, SystemEvent event) {
		
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
	public Collection<PropertyCategory> getPropertyTemplate() {
		return getRepository().getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertiesForAction(
			TriggerAction action) {
		return getRepository().getPropertyCategories(action);
	}
}
