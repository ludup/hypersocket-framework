package com.hypersocket.triggers.actions.ip;

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

import com.hypersocket.events.EventService;
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
public class BlockIPTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(BlockIPTask.class);
	
	public static final String RESOURCE_BUNDLE = "BlockIPTask";
	
	public static final String RESOURCE_KEY = "blockIP";
	
	Map<String,String> blockedIPUnblockSchedules = new HashMap<String,String>();
	Set<String> blockedIps = new HashSet<String>();
	
	@Autowired
	BlockIPTaskRepository repository; 
	
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
	
	@Autowired
	EventService eventService; 
	
	@PostConstruct
	private void postConstruct() {
	
		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);
		
		eventService.registerEvent(BlockedIPResult.class, RESOURCE_BUNDLE);
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
		
		String ipAddress = processTokenReplacements(repository.getValue(task, "block.ip"), event);
		int val = repository.getIntValue(task, "block.length");
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Blocking IP address "  + ipAddress);
			}
			
			if(blockedIps.contains(ipAddress) && blockedIPUnblockSchedules.containsKey(ipAddress)) {
				if(log.isInfoEnabled()) {
					log.info(ipAddress + " is already blocked. Rescheduling to new parameters");
				}
				
				String scheduleId = blockedIPUnblockSchedules.get(ipAddress);
				
				if(log.isInfoEnabled()) {
					log.info("Cancelling existing schedule for " + ipAddress);
				}
				
				try {
					schedulerService.cancelNow(scheduleId);
				} catch (Exception e) {
					log.error("Failed to cancel unblock schedule for " + ipAddress, e);
				}
				
			}
			
			server.blockAddress(ipAddress);
			
			if(log.isInfoEnabled()) {
				log.info("Blocked IP address " + ipAddress);
			}
			
			blockedIps.add(ipAddress);
			
			if(val > 0) {
				
				if(log.isInfoEnabled()) {
					log.info("Scheduling unblock for IP address " + ipAddress + " in " + val + " minutes");
				}
				
				PermissionsAwareJobData data = new PermissionsAwareJobData(event.getCurrentRealm());
				data.put("addr", ipAddress);
				
				String scheduleId = schedulerService.scheduleIn(UnblockIPJob.class, data, val * 60000);
				
				blockedIPUnblockSchedules.put(ipAddress, scheduleId);
			}
			return new BlockedIPResult(this, event.getCurrentRealm(), task, ipAddress, val);
		} catch (UnknownHostException | SchedulerException e) {
			log.error("Failed to fully process block IP request for " + ipAddress, e);
			return new BlockedIPResult(this, e, event.getCurrentRealm(), task, ipAddress, val);
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
