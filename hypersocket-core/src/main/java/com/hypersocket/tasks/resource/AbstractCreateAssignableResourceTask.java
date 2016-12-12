package com.hypersocket.tasks.resource;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.AssignableResource;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;

public abstract class AbstractCreateAssignableResourceTask<T extends AssignableResource> extends AbstractTaskProvider {

	@Autowired
	TaskProviderService taskService; 
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);
	}
	
	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event) throws ValidationException {
		
		AbstractAssignableResourceService<T> service = getService();
		Map<String,String> properties = new HashMap<String,String>();
		
		for(PropertyCategory c : service.getResourceTemplate()) {
			for(AbstractPropertyTemplate t : c.getTemplates()) {
				properties.put(t.getResourceKey(), 
					processTokenReplacements(getRepository().getValue(task, t.getResourceKey()), event));
			}
		}

		return createResourceFromProperties(task, properties);
	}

	protected abstract TaskResult createResourceFromProperties(Task task, Map<String, String> properties);

	public abstract AbstractAssignableResourceService<T> getService();


}
