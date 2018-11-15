package com.hypersocket.tasks.user;

import java.util.List;

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
import com.hypersocket.realm.events.AccountEnabledEvent;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class EnableAccountTask extends AbstractAccountTask {

	public static final String ACTION_ENABLE_ACCOUNT = "enableAccount";

	@Autowired
	private EnableAccountTaskRepository taskRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private RealmService realmService;

	public EnableAccountTask() {

	}

	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);
		eventService.registerEvent(AccountEnabledEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);

	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_ENABLE_ACCOUNT };
	}

	@Override
	protected AbstractTaskResult doExecute(Principal p, final Task task, final Realm currentRealm,
			final List<SystemEvent> event) throws ValidationException {
		try {
			realmService.enableAccount(p);
			return new EnableAccountTaskResult(this, currentRealm, task);
		} catch (Exception e) {
			return new EnableAccountTaskResult(this, currentRealm, task, e);
		}
	}

	public String[] getResultResourceKeys() {
		return new String[] { DisableAccountTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return taskRepository;
	}

}
