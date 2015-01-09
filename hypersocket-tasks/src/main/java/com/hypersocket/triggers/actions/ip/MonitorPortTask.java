package com.hypersocket.triggers.actions.ip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.ValidationException;

@Component
public class MonitorPortTask extends AbstractTaskProvider {
	
static Logger log = LoggerFactory.getLogger(BlockIPTask.class);
	
	public static final String RESOURCE_BUNDLE = "MonitorPortTask";
	
	public static final String RESOURCE_KEY = "monitorPort";
	
	Map<String,String> blockedIPUnblockSchedules = new HashMap<String,String>();
	Set<String> blockedIps = new HashSet<String>();
	
	@Autowired
	MonitorPortTaskRepository repository; 
	
	@Autowired
	HypersocketServer server;
	
	@Autowired
	TriggerResourceService triggerService; 
	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	SchedulerService schedulerService; 
	
	@Autowired
	TaskProviderService taskService; 
	
	@PostConstruct
	private void postConstruct() {
	
		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerActionProvider(this);
	}
	
	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { RESOURCE_KEY };
	}

	

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {
		if(parameters.containsKey("block.ip")) {
			throw new ValidationException("IP address required");
		}

	}

	@Override
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
