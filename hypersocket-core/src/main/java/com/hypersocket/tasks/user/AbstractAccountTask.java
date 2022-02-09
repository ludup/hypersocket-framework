package com.hypersocket.tasks.user;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.resource.ResourceNotFoundException;
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
	public final TaskResult execute(final Task task, final Realm currentRealm, final List<SystemEvent> events)
			throws ValidationException {

		String principalName = processTokenReplacements(getRepository().getValue(task, "accountTask.principalName"), events);
		boolean checkName = getRepository().getBooleanValue(task, "accountTask.checkPrincipalName");
		boolean checkID = getRepository().getBooleanValue(task, "accountTask.checkPrincipalId");
		boolean checkEmail = getRepository().getBooleanValue(task, "accountTask.checkPrincipalEmail");
		
		try {
			Principal p = null;
			
			if(checkName) {
				p = realmService.getPrincipalByName(currentRealm, principalName, getType(task));
			}
			
			if(Objects.isNull(p) && checkID && NumberUtils.isCreatable(principalName)) {
				p = realmService.getPrincipalById(Long.parseLong(principalName));
			}
			
			if(Objects.isNull(p) && checkEmail) {
				p = realmService.getPrincipalByEmail(currentRealm, principalName);
			}
			
			if(p == null)
				throw new ResourceNotFoundException(RealmServiceImpl.RESOURCE_BUNDLE, "accountTask.noSuchPrincipal", principalName);
			return doExecute(p, task, currentRealm, events);
		} catch (NumberFormatException | ResourceNotFoundException e) {
			return getFailedResult(task, currentRealm, events, e, principalName);
		}
	}

	protected abstract TaskResult getFailedResult(Task task, Realm currentRealm, List<SystemEvent> events, Exception e, String principalNamme);

	protected abstract TaskResult doExecute(Principal p, final Task task, final Realm currentRealm,
			final List<SystemEvent> event) throws ValidationException;

	public String getResultResourceKey() {
		return AlertEvent.EVENT_RESOURCE_KEY;
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
