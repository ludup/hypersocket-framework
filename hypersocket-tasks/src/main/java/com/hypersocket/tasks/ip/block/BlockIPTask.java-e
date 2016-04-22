package com.hypersocket.tasks.ip.block;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.hypersocket.ip.IPRestrictionService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.ip.unblock.UnblockIPJob;
import com.hypersocket.triggers.MultipleTaskResults;
import com.hypersocket.triggers.AbstractTaskResult;
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
	IPRestrictionService ipRestrictionService;
	
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
		eventService.registerEvent(BlockedIPTempResult.class, RESOURCE_BUNDLE);
		eventService.registerEvent(BlockedIPPermResult.class, RESOURCE_BUNDLE);
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
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {
		
		String[] values = ResourceUtils.explodeValues(repository.getValue(task, "block.ip"));
		int val = repository.getIntValue(task, "block.length");
		List<BlockedIPResult> results = new ArrayList<BlockedIPResult>();
		
		for(String value : values) {
			for(String ipAddress : ResourceUtils.explodeValues(processTokenReplacements(value, event))) {
				try {
					
					if(log.isInfoEnabled()) {
						log.info("Blocking IP address "  + ipAddress);
					}
					
					if(ipRestrictionService.isBlockedAddress(ipAddress)) {
						if(log.isInfoEnabled()) {
							log.info(ipAddress + " is already blocked.");
						}
						
						if(val > 0) {
							results.add(new BlockedIPTempResult(this, new IOException(ipAddress + " is already blocked"), currentRealm, task, ipAddress, val));
						} else {
							results.add(new BlockedIPPermResult(this, new IOException(ipAddress + " is already blocked"), currentRealm, task, ipAddress));
						}
						
						
					} else {
					
						ipRestrictionService.blockIPAddress(ipAddress, val==0);
						
						if(log.isInfoEnabled()) {
							log.info("Blocked IP address " + ipAddress);
						}
						
						blockedIps.add(ipAddress);
						
						if(val > 0) {
							
							if(log.isInfoEnabled()) {
								log.info("Scheduling unblock for IP address " + ipAddress + " in " + val + " minutes");
							}
							
							PermissionsAwareJobData data = new PermissionsAwareJobData(currentRealm, "unblockIP");
							data.put("addr", ipAddress);
		
							String scheduleId = schedulerService.scheduleIn(UnblockIPJob.class, data, val * 60000, 0);
							
							blockedIPUnblockSchedules.put(ipAddress, scheduleId);
												
							results.add(new BlockedIPTempResult(this, currentRealm, task, ipAddress, val));
						} else {
							
							results.add(new BlockedIPPermResult(this, currentRealm, task, ipAddress));
						}
						
					}
				} catch (UnknownHostException | SchedulerException e) {
					log.error("Failed to fully process block IP request for " + ipAddress, e);
					if(val > 0) {
						results.add(new BlockedIPTempResult(this, e, currentRealm, task, ipAddress, val));
					} else {
						results.add(new BlockedIPPermResult(this, e, currentRealm, task, ipAddress));
					}
				}
			}
		}
		
		return new MultipleTaskResults(this, currentRealm, task, results.toArray(new AbstractTaskResult[0]));
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { BlockedIPResult.EVENT_RESOURCE_KEY, BlockedIPTempResult.EVENT_RESOURCE_KEY, BlockedIPPermResult.EVENT_RESOURCE_KEY };
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
