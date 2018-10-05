package com.hypersocket.triggers.conditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerConditionProvider;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.ValidationException;

@Component
public class ComparisonConditionProvider implements TriggerConditionProvider {

	@Autowired
	TriggerResourceService triggerService; 
	
	Map<String,Condition> supportedConditions = new HashMap<String,Condition>();
	
	@PostConstruct
	private void postConstruct() {
		
		supportedConditions.put("condition.equals", new EqualsCondition());
		supportedConditions.put("condition.notEquals", new NotEqualsCondition());
		supportedConditions.put("condition.startsWith", new StartsWithCondition());
		supportedConditions.put("condition.endsWith", new EndsWithCondition());
		supportedConditions.put("condition.doesNotStartWith", new DoesNotStartsWithCondition());
		supportedConditions.put("condition.doesNotEndWith", new DoesNotEndsWithCondition());
		supportedConditions.put("condition.contains", new ContainsCondition());
		supportedConditions.put("condition.isEmpty", new IsEmptyCondition());
		supportedConditions.put("condition.isNotEmpty", new IsNotEmptyCondition());
		supportedConditions.put("condition.isNumeric", new IsNotEmptyCondition());
		supportedConditions.put("condition.isTrue", new IsTrueCondition());
		supportedConditions.put("condition.isNotTrue", new IsNotTrueCondition());
		supportedConditions.put("condition.greaterThan", new GreaterThanCondition());
		supportedConditions.put("condition.greaterThanOrEquals", new GreaterThanOrEqualsCondition());
		supportedConditions.put("condition.lessThan", new LessThanCondition());
		supportedConditions.put("condition.lessThanOrEquals", new LessThanOrEqualsCondition());
		supportedConditions.put("condition.notGreaterThan", new NotGreaterThanCondition());
		supportedConditions.put("condition.notGreaterThanOrEquals", new NotGreaterThanOrEqualsCondition());
		supportedConditions.put("condition.notLessThan", new NotLessThanCondition());
		supportedConditions.put("condition.notLessThanOrEquals", new NotLessThanOrEqualsCondition());
		
		triggerService.registerConditionProvider(this);
		
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return supportedConditions.keySet().toArray(new String[0]);
	}

	@Override
	public boolean checkCondition(TriggerCondition condition, TriggerResource trigger,
			SystemEvent event, List<SystemEvent> sourceEvents) throws ValidationException {
		if(!supportedConditions.containsKey(condition.getConditionKey()))
			throw new ValidationException("ComparisonConditionProvider does not support the condition key " + condition.getConditionKey());
		return supportedConditions.get(condition.getConditionKey()).checkCondition(condition, trigger, sourceEvents);
	}

}
