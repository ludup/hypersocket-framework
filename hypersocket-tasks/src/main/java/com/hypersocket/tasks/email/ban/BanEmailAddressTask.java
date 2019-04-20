package com.hypersocket.tasks.email.ban;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.user.AbstractAccountTask;
import com.hypersocket.triggers.ValidationException;

@Component
public class BanEmailAddressTask extends AbstractAccountTask {

	public static final String TASK_RESOURCE_KEY = "banEmailAddressTask";

	public static final String RESOURCE_BUNDLE = "BanEmailAddressTask";
	
	@Autowired
	private BanEmailAddressTaskRepository repository;

	@Autowired
	private TaskProviderService taskService;

	@Autowired
	private EventService eventService;

	@Autowired
	private I18NService i18nService; 

	@Autowired
	private RealmService realmService;
	
	public BanEmailAddressTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(BanEmailAddressTaskResult.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { TASK_RESOURCE_KEY };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

	}
	
	public String getResultResourceKey() {
		return BanEmailAddressTaskResult.EVENT_RESOURCE_KEY;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

	@Override
	protected TaskResult getFailedResult(Task task, Realm currentRealm, List<SystemEvent> events, Exception e) {
		return new BanEmailAddressTaskResult(this, e, currentRealm, task);
	}

	@Override
	protected TaskResult doExecute(Principal p, Task task, Realm currentRealm, List<SystemEvent> events)
			throws ValidationException {
		
		boolean banned = realmService.getUserPropertyBoolean(p, "user.bannedEmail");
		
		if(!banned) {
			realmService.setUserPropertyBoolean(p, "user.bannedEmail", true);
			return new BanEmailAddressTaskResult(this, true, currentRealm, task, p.getEmail(), p.getName());
		} else {
			return new BanEmailAddressTaskResult(this, false, currentRealm, task, p.getEmail(), p.getName());
		}
	}

}
