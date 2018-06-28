package com.hypersocket.tasks.phone;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class ParsePhoneNumberTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "parsePhoneNumberTask";

	public static final String RESOURCE_BUNDLE = "ParsePhoneNumberTask";
	
	@Autowired
	ParsePhoneNumberTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 

	public ParsePhoneNumberTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(ParsePhoneNumberTaskResult.class,
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

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		String phoneNumber = processTokenReplacements(repository.getValue(task, "parseNumber.input"), event);
        String countryCode = processTokenReplacements(repository.getValue(task, "parseNumber.countryCode"), event);
        
		try {
			PhoneNumber parsed = PhoneNumberUtil.getInstance().parse(phoneNumber, countryCode);
			return new ParsePhoneNumberTaskResult(this, true, currentRealm, task, parsed);
			
		} catch (Throwable e) {
			return new ParsePhoneNumberTaskResult(this, e, currentRealm, task);
		}
        
		
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { ParsePhoneNumberTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

}
