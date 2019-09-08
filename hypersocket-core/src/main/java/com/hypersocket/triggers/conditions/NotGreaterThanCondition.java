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
		String value2 = TriggerAttributeHelper.processEventReplacements(condition.getConditionValue(), sourceEvents);
		try {
			return !(Long.valueOf(value) > Long.parseLong(value2));
		}
		catch(NumberFormatException nfe) {
			try {
				return !(Double.valueOf(value) > Double.parseDouble(value2));
			}
			catch(NumberFormatException nfe2) {
				return !(value.compareTo(value2) > 0);
			}	
		}
	}

	@Override
	public boolean isValueRequired() {
		return true;
	}
}
