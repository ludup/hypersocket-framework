package com.hypersocket.triggers.conditions;

import java.util.List;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.ValidationException;

public class NotGreaterThanCondition implements Condition {

	@Override
	public boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			List<SystemEvent> sourceEvents) throws ValidationException {

		String value = TriggerAttributeHelper.getAttribute(condition.getAttributeKey(), sourceEvents);
		try {
			return !(Long.valueOf(value) > Long.parseLong(condition.getConditionValue()));
		}
		catch(NumberFormatException nfe) {
			try {
				return !(Double.valueOf(value) > Double.parseDouble(condition.getConditionValue()));
			}
			catch(NumberFormatException nfe2) {
				return !(value.compareTo(condition.getConditionValue()) > 0);
			}	
		}
	}

	@Override
	public boolean isValueRequired() {
		return true;
	}
}
