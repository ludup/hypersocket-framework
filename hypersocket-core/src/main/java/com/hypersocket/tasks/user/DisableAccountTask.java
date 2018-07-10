package com.hypersocket.tasks.user;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.realm.events.AccountDisabledEvent;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.alert.AlertEvent;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class DisableAccountTask extends AbstractTaskProvider {

	public static final String ACTION_DISABLE_ACCOUNT = "disableAccount";

	public static final String ATTR_PRINCIPAL_ID = "alert.principalId";
	public static final String ATTR_PRINCIPAL_NAME = "alert.principalName";

	@Autowired
	private EnableAccountTaskRepository taskRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private RealmService realmService;

	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);
		eventService.registerEvent(AccountDisabledEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);

	}

	@Override
	public String getResourceBundle() {
		return RealmServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_DISABLE_ACCOUNT };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters) throws ValidationException {
		boolean haveId = StringUtils.isNotBlank(parameters.get(ATTR_PRINCIPAL_ID));
		boolean haveName = StringUtils.isNotBlank(parameters.get(ATTR_PRINCIPAL_NAME));
		if ((!haveId && !haveName) || (haveId && haveName))
			throw new ValidationException("error.enableAccount.mustProvideIdOrName");
	}

	@Override
	public AbstractTaskResult execute(final Task task, final Realm currentRealm, final List<SystemEvent> event)
			throws ValidationException {

		Principal p;
		Long id = taskRepository.getLongValue(task, ATTR_PRINCIPAL_ID);
		if (id == 0) {
			p = realmService.getPrincipalByName(currentRealm, "", PrincipalType.USER);
		} else
			p = realmService.getPrincipalById(id);

		try {
			realmService.disableAccount(p);
		} catch (Exception e) {
		}
		return null;
	}

	public String[] getResultResourceKeys() {
		return new String[] { AlertEvent.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return taskRepository;
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

}
