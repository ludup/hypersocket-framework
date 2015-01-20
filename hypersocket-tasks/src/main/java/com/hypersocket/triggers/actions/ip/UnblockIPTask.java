package com.hypersocket.triggers.actions.ip;

import java.net.UnknownHostException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
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
import com.hypersocket.triggers.ValidationException;

@Component
public class UnblockIPTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(UnblockIPTask.class);
	
	public static final String RESOURCE_BUNDLE = "BlockIPTask";
	
	public static final String RESOURCE_KEY = "unblockIP";
	
	
	@Autowired
	BlockIPTask blockTask; 
	
	@Autowired
	UnblockIPTaskRepository repository; 
	
	@Autowired
	HypersocketServer server;
	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	SchedulerService schedulerService; 
	
	@Autowired
	TaskProviderService taskService; 
	
	@PostConstruct
	private void postConstruct() {
	
		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);
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
		if(parameters.containsKey("unblock.ip")) {
			throw new ValidationException("IP address required");
		}
	}

	@Override
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {
		
		String ipAddress = processTokenReplacements(repository.getValue(task, "unblock.ip"), event);
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Unblocking IP address "  + ipAddress);
			}
			
			server.unblockAddress(ipAddress);
			
			blockTask.notifyUnblock(ipAddress, false);
			
			return new UnblockedIPResult(this, event.getCurrentRealm(), task, ipAddress);
		} catch (UnknownHostException e) {
			log.error("Failed to fully process block IP request for " + ipAddress, e);
			return new UnblockedIPResult(this, e, event.getCurrentRealm(), task, ipAddress);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
}
