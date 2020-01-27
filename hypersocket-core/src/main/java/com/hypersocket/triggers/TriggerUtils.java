package com.hypersocket.triggers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;

@Component
public class TriggerUtils {

	@Autowired
	private TriggerResourceRepository triggerRepository;
	
	@Autowired
	private TaskProviderService taskService; 
	
	@SuppressWarnings("unchecked")
	public void createTrigger(Realm realm, 
			String name, String eventResourceKey, boolean allRealms, 
			TriggerResultType onResult, String taskResourceKey, Map<String,String> taskProperties, TriggerCondition... conditions ) throws ResourceException {
		TriggerResource trigger = new TriggerResource();

		trigger.setRealm(realm);
		trigger.setName(name);
		trigger.setTriggerType(TriggerType.TRIGGER);
		trigger.setEvent(eventResourceKey);
		trigger.setAllRealms(allRealms);
		trigger.setResult(onResult);
		trigger.setResourceKey(taskResourceKey);
	
		for(TriggerCondition condition : conditions) {
			condition.setTrigger(trigger);
			trigger.getConditions().add(condition);
		}
		
		triggerRepository.saveResource(trigger, taskProperties,
				new TransactionAdapter<TriggerResource>() {
					@Override
					public void afterOperation(TriggerResource resource, Map<String, String> properties) {
						TaskProvider provider = taskService.getTaskProvider(resource.getResourceKey());
						provider.getRepository().setValues(resource, properties);
					}
		});
	}
}
