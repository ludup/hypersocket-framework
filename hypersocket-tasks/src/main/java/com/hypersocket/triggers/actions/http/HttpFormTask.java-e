package com.hypersocket.triggers.actions.http;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.ValidationException;

@Component
public class HttpFormTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(HttpFormTask.class);
	
	public static final String RESOURCE_BUNDLE = "HttpFormTask";
	
	public static final String RESOURCE_KEY = "httpForm";
	
	
	@Autowired
	HttpFormTaskRepository repository;
	
	
	@Autowired
	TriggerResourceService triggerService; 
	
	@Autowired
	I18NService i18nService;
	
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
		
		String method = repository.getValue(task, "httpForm.method");
		String url = repository.getValue(task, "httpForm.url");
		String variables = repository.getValue(task, "httpForm.variables");
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Method "  + method);
				log.info("URL "  + url);
				log.info("variables "  + variables);
			}
			
			
			return new HttpFormTaskResult(this, event.getCurrentRealm(), task, method, url, variables);
		} catch (UnknownHostException | SchedulerException e) {
			log.error("Failed to fully process block IP request for " + ipAddress, e);
			return new BlockedIPResult(this, e, event.getCurrentRealm(), task, ipAddress);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	public void notifyUnblock(String addr, boolean onSchedule) {
		
		blockedIps.remove(addr);
		String scheduleId = blockedIPUnblockSchedules.remove(addr);
		
		if(!onSchedule && scheduleId!=null) {
			try {
				schedulerService.cancelNow(scheduleId);
			} catch (SchedulerException e) {
				log.error("Failed to cancel unblock job for ip address " + addr.toString(), e);
			}
		}
		
	}

}
