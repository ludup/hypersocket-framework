package com.hypersocket.tasks.principal.legacy;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class ImportPrincipalLegacyIDTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "importPrincipalLegacyIDTask";

	public static final String RESOURCE_BUNDLE = "ImportPrincipalLegacyIDTask";
	
	@Autowired
	ImportPrincipalLegacyIDTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 

	@Autowired
	PrincipalRepository principalRepository; 
	
	public ImportPrincipalLegacyIDTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(ImportPrincipalLegacyIDTaskResult.class,
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
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> events)
			throws ValidationException {

		try {
			String reference = processTokenReplacements(repository.getValue(task, "importPrincipalLegacyID.reference"), events);
			Long legacyId = Long.parseLong(processTokenReplacements(repository.getValue(task, "importPrincipalLegacyID.legacyId"), events));
			
			Principal principal = principalRepository.getPrincipalByReference(reference, currentRealm);
			if(Objects.isNull(principal)) {
				return new ImportPrincipalLegacyIDTaskResult(this, 
						new Exception(String.format("Cannot find principal from reference %s", reference)),
						currentRealm, task);
			} else {
				principal.setLegacyId(legacyId);
				principalRepository.saveResource(principal);
				return new ImportPrincipalLegacyIDTaskResult(this, true, currentRealm, task);
			}
		} catch(Throwable t) {
			return new ImportPrincipalLegacyIDTaskResult(this, t, currentRealm, task);
		}
		
	}
	
	public String getResultResourceKey() {
		return ImportPrincipalLegacyIDTaskResult.EVENT_RESOURCE_KEY;
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
