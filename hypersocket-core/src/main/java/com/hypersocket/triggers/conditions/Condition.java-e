package com.hypersocket.triggers.conditions;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.ValidationException;

public interface Condition {

	boolean checkCondition(TriggerCondition condition, 
			TriggerResource trigger,
			SystemEvent event) throws ValidationException;
	
	boolean isValueRequired();
}
