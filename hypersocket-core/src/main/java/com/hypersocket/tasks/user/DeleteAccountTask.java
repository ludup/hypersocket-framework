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
import com.hypersocket.realm.events.AccountDisabledEvent;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class DeleteAccountTask extends AbstractAccountTask {


	public static final String ACTION_DELETE_ACCOUNT = "deleteAccount";

	@Autowired
	private DeleteAccountTaskRepository taskRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private RealmService realmService;

	public DeleteAccountTask() {

	}

	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);
		eventService.registerEvent(AccountDisabledEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);

	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_DELETE_ACCOUNT };
	}

	@Override
	protected AbstractTaskResult doExecute(Principal p, final Task task, final Realm currentRealm, final List<SystemEvent> event)
			throws ValidationException {
		try {
			realmService.deleteUser(currentRealm, p);
			return new DeleteAccountTaskResult(this, currentRealm, task);
		} catch (Exception e) {
			return new DeleteAccountTaskResult(this, currentRealm, task, e, p.getPrincipalName());
		}
	}

	public String getResultResourceKey() {
		return DeleteAccountTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return taskRepository;
	}

	@Override
	protected TaskResult getFailedResult(Task task, Realm currentRealm, List<SystemEvent> events, Exception e, String principalName) {
		return new DeleteAccountTaskResult(this, currentRealm, task, e, principalName);
	}


}
