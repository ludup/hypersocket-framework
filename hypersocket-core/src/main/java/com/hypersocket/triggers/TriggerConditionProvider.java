package com.hypersocket.triggers;

import java.util.List;

import com.hypersocket.events.SystemEvent;

public interface TriggerConditionProvider {

	String getResourceBundle();

	String[] getResourceKeys();

	boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			SystemEvent event, List<SystemEvent> sourceEvents) throws ValidationException;

}
