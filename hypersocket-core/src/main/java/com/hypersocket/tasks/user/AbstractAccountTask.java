package com.hypersocket.tasks.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.alert.AlertEvent;
import com.hypersocket.triggers.ValidationException;

public abstract class AbstractAccountTask extends AbstractTaskProvider {

	@Autowired
	private RealmService realmService;


	protected AbstractAccountTask() {

	}

	@Override
	public String getResourceBundle() {
		return RealmServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {

	}

	@Override
	public final TaskResult execute(final Task task, final Realm currentRealm, final SystemEvent event)
			throws ValidationException {
		
		String principalName = processTokenReplacements(getRepository().getValue(task, "accountTask.principalName"), event);
		Principal p = realmService.getPrincipalByName(currentRealm, principalName, PrincipalType.USER);

		return doExecute(p, task, currentRealm, event);
	}

	protected abstract TaskResult doExecute(Principal p, final Task task, final Realm currentRealm,
			final SystemEvent event) throws ValidationException;

	public String[] getResultResourceKeys() {
		return new String[] { AlertEvent.EVENT_RESOURCE_KEY };
	}

	@Override
	public void taskCreated(Task task) {
	}

	@Override
	public void taskUpdated(Task task) {
	}

	@Override
	public void taskDeleted(Task task) {

	}

	@Override
	public boolean supportsAutomation() {
		return true;
	}

	@Override
	public boolean supportsTriggers() {
		return true;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	protected PrincipalType getType(final Task task) {
		return PrincipalType.USER;
	}
}
