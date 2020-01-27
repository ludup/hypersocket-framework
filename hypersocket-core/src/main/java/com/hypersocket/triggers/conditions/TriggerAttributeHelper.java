package com.hypersocket.triggers.conditions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.replace.ReplacementUtils;

public class TriggerAttributeHelper {

	private static Pattern eventPattern;
	
	static {
		eventPattern = Pattern.compile("event(\\d+):(.*)");
	}
	
	public static String getAttribute(String attribute, List<SystemEvent> sourceEvents) {
		
		if(sourceEvents.isEmpty()) {
			return null;
		}
		SystemEvent lastEvent = sourceEvents.get(sourceEvents.size()-1);
		if(lastEvent.hasAttribute(attribute)) {
			return lastEvent.getAttribute(attribute);
		}
		
		Matcher m = eventPattern.matcher(attribute);
		if(m.matches()) {
			int index = Integer.parseInt(m.group(1));
			String realAttribute = m.group(2);
			return  sourceEvents.get(index).getAttribute(realAttribute);
		}

		return null;
	}
	
	public static String processEventReplacements(String value, List<SystemEvent> sourceEvents) {
		return ReplacementUtils.processTokenReplacements(value, new EventTokenResolver(sourceEvents));
	}

}
