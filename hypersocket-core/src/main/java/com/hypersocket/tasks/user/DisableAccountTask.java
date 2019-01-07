package com.hypersocket.tasks.user;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.realm.events.AccountDisabledEvent;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class DisableAccountTask extends AbstractAccountTask {

	public static final String ACTION_DISABLE_ACCOUNT = "disableAccount";

	@Autowired
	private DisableAccountTaskRepository taskRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private RealmService realmService;

	public DisableAccountTask() {

	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);
		eventService.registerEvent(AccountDisabledEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);

	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_DISABLE_ACCOUNT };
	}

	@Override
	protected AbstractTaskResult doExecute(Principal p, final Task task, final Realm currentRealm, final SystemEvent event)
			throws ValidationException {
		try {
			realmService.disableAccount(p);
			return new DisableAccountTaskResult(this, currentRealm, task);
		} catch (Exception e) {
			return new DisableAccountTaskResult(this, currentRealm, task, e);
		}
	}

	public String[] getResultResourceKeys() {
		return new String[] { DisableAccountTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return taskRepository;
	}

	@Override
	protected TaskResult getFailedResult(Task task, Realm currentRealm, SystemEvent event, Exception e) {
		return new DisableAccountTaskResult(this, currentRealm, task, e);
	}


}
