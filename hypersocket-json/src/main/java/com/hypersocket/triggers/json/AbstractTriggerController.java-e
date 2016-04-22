package com.hypersocket.triggers.json;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResourceService;

public class AbstractTriggerController extends ResourceController {

	
	@Autowired
	TriggerResourceService triggerService;
	
	protected void processConditions(TriggerResourceUpdate resource, List<TriggerCondition> allConditions,
			List<TriggerCondition> anyConditions) throws AccessDeniedException {
		
		for (TriggerConditionUpdate c : resource.getAllConditions()) {
			TriggerCondition cond;
			if (resource.getId()==null || c.getId()==null) {
				cond = new TriggerCondition();
			} else {
				cond = triggerService.getConditionById(c.getId());
			}
			cond.setAttributeKey(c.getAttributeKey());
			cond.setConditionKey(c.getConditionKey());
			cond.setConditionValue(c.getConditionValue());
			allConditions.add(cond);
		}

		for (TriggerConditionUpdate c : resource.getAnyConditions()) {
			TriggerCondition cond;
			if (resource.getId()==null || c.getId() == null) {
				cond = new TriggerCondition();
			} else {
				cond = triggerService.getConditionById(c.getId());
			}
			cond.setAttributeKey(c.getAttributeKey());
			cond.setConditionKey(c.getConditionKey());
			cond.setConditionValue(c.getConditionValue());
			anyConditions.add(cond);
		}
		
	}
}
