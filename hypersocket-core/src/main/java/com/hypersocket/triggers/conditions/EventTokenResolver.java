package com.hypersocket.triggers.conditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.utils.ITokenResolver;

public class EventTokenResolver implements ITokenResolver {

	static Pattern eventPattern;
	
	static {
		eventPattern = Pattern.compile("event(\\d+):(.*)");
	}
	
	List<SystemEvent> events;
	public EventTokenResolver(List<SystemEvent> events) {
		this.events = events;
	}
	
	@Override
	public String resolveToken(String tokenName) {
		if(events.isEmpty()) {
			return null;
		}
		SystemEvent lastEvent = events.get(events.size()-1);
		if(lastEvent.hasAttribute(tokenName)) {
			return lastEvent.getAttribute(tokenName);
		}
		
		Matcher m = eventPattern.matcher(tokenName);
		if(m.matches()) {
			int index = Integer.parseInt(m.group(1));
			String realAttribute = m.group(2);
			return  events.get(index).getAttribute(realAttribute);
		}

		return null;
	}

	@Override
	public Map<String, Object> getData() {
		Map<String,Object> result = new HashMap<>();
		for(SystemEvent e : events) {
			result.putAll(e.getAttributes());
		}
		return result;
	}

}
