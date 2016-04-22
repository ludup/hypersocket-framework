package com.hypersocket.tasks.ip.unblock;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.ip.IPRestrictionService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.ip.block.BlockIPTask;
import com.hypersocket.triggers.MultipleTaskResults;
import com.hypersocket.triggers.AbstractTaskResult;
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
	IPRestrictionService ipRestrictionService;
	
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
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {
		
		String[] values = ResourceUtils.explodeValues(repository.getValue(task, "unblock.ip"));
		
		List<UnblockedIPResult> results = new ArrayList<UnblockedIPResult>();
		
		for(String value : values) {
			for(String ipAddress : ResourceUtils.explodeValues(processTokenReplacements(value, event))) {
				try {
					
					if(log.isInfoEnabled()) {
						log.info("Unblocking IP address "  + ipAddress);
					}
					
					ipRestrictionService.unblockIPAddress(ipAddress);
					
					blockTask.notifyUnblock(ipAddress, false);
					
					results.add(new UnblockedIPResult(this, currentRealm, task, ipAddress));
				} catch (UnknownHostException e) {
					log.error("Failed to fully process block IP request for " + ipAddress, e);
					results.add(new UnblockedIPResult(this, e, currentRealm, task, ipAddress));
				}
			}
		}
		
		return new MultipleTaskResults(this, currentRealm, task, results.toArray(new AbstractTaskResult[0]));
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { UnblockedIPResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
}
