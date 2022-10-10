package com.hypersocket.tasks.user;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.realm.events.AccountEnabledEvent;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class UpdateAccountAddressesTask extends AbstractAccountTask {

	public static final String ACTION_UPDATE_ACCOUNT_ADDRESSES = "updateAccountAddresses";

	@Autowired
	private UpdateAccountAddressesTaskRepository taskRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService;
	@Autowired
	private RealmService realmService;

	public UpdateAccountAddressesTask() {
	}

	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);
		eventService.registerEvent(AccountEnabledEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);

	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { ACTION_UPDATE_ACCOUNT_ADDRESSES };
	}

	@Override
	protected AbstractTaskResult doExecute(Principal p, final Task task, final Realm currentRealm,
			final List<SystemEvent> event) throws ValidationException {
		try {
			String s = taskRepository.getValue(task, "updateAccountAddresses.primaryEmail");
			boolean changed = false;
			String was;
			if (StringUtils.isNotBlank(s)) {
				was = p.getPrimaryEmail();
				p.setPrimaryEmail(processTokenReplacements(s, event));
				changed = changed || !Objects.equals(p.getPrimaryEmail(), was);
			}
			if (p instanceof UserPrincipal) {
				UserPrincipal<?> up = (UserPrincipal<?>) p;
				s = taskRepository.getValue(task, "updateAccountAddresses.primaryMobile");
				if (StringUtils.isNotBlank(s)) {
					was = up.getMobile();
					up.setMobile(processTokenReplacements(s, event));
					changed = changed || !Objects.equals(up.getMobile(), was);
				}
				s = taskRepository.getValue(task, "updateAccountAddresses.secondaryEmail");
				if (StringUtils.isNotBlank(s))  {
					was = up.getSecondaryEmail();
					up.setSecondaryEmail(processTokenReplacements(s, event));
					changed = changed || !Objects.equals(up.getSecondaryEmail(), was);
				}
				s = taskRepository.getValue(task, "updateAccountAddresses.secondaryMobile");
				if (StringUtils.isNotBlank(s)) {
					was = up.getSecondaryMobile();
					up.setSecondaryMobile(processTokenReplacements(s, event));
					changed = changed || !Objects.equals(up.getSecondaryMobile(), was);
				}
			}
			if(changed) {
				realmService.updateUser(p.getRealm(), p, p.getName(), new HashMap<String, String>(), null);
			}
			return new UpdateAccountAddressesTaskResult(this, currentRealm, p.getName(), task);
		} catch (Exception e) {
			return new UpdateAccountAddressesTaskResult(this, currentRealm, task, e);
		}
	}

	public String getResultResourceKey() {
		return UpdateAccountAddressesTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return taskRepository;
	}

	@Override
	protected TaskResult getFailedResult(Task task, Realm currentRealm, List<SystemEvent> events, Exception e, String principalName) {
		return new UpdateAccountAddressesTaskResult(this, currentRealm, task, e);
	}

}
