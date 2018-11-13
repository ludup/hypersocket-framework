package com.hypersocket.tasks.user;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

	//

	private String principalIdKey;
	private String principalNameKey;

	protected AbstractAccountTask(String principalIdKey, String principalNameKey) {
		this.principalIdKey = principalIdKey;
		this.principalNameKey = principalNameKey;
	}

	@Override
	public String getResourceBundle() {
		return RealmServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		boolean haveId = StringUtils.isNotBlank(parameters.get(principalIdKey));
		boolean haveName = StringUtils.isNotBlank(parameters.get(principalNameKey));
		if ((!haveId && !haveName) || (haveId && haveName))
			throw new ValidationException("error.accountTask.mustProvideIdOrName");
	}

	@Override
	public final TaskResult execute(final Task task, final Realm currentRealm, final List<SystemEvent> event)
			throws ValidationException {
		Principal p;
		Long id = getRepository().getLongValue(task, principalIdKey);
		String name = getRepository().getValue(task, principalNameKey);
		if (id == null || id == 0) {
			p = realmService.getPrincipalByName(currentRealm, name, getType(task));
		} else
			p = realmService.getPrincipalById(id);

		if(p==null) {
			if (id == null || id == 0) 
				throw new ValidationException(String.format("Principal %s not found", name));
			else
				throw new ValidationException(String.format("Principal %d not found", id));
		}
		return doExecute(p, task, currentRealm, event);
	}

	protected abstract TaskResult doExecute(Principal p, final Task task, final Realm currentRealm,
			final List<SystemEvent> event) throws ValidationException;

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
