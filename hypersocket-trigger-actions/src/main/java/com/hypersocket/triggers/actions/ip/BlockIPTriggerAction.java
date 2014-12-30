package com.hypersocket.triggers.actions.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.CommonAttributes;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.triggers.AbstractActionProvider;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerAction;
import com.hypersocket.triggers.TriggerActionProvider;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.ValidationException;

@Component
public class BlockIPTriggerAction extends AbstractActionProvider implements
		TriggerActionProvider {

	static Logger log = LoggerFactory.getLogger(BlockIPTriggerAction.class);
	
	public static final String RESOURCE_BUNDLE = "BlockIPTriggerAction";
	
	public static final String RESOURCE_KEY = "blockIP";
	
	@Autowired
	BlockIPTriggerActionRepository repository; 
	
	@Autowired
	HypersocketServer server;
	
	@Autowired
	TriggerResourceService triggerService; 
	
	@Autowired
	I18NService i18nService;
	
	@Autowired
	SchedulerService schedulerService; 
	
	@PostConstruct
	private void postConstruct() {
	
		i18nService.registerBundle(RESOURCE_BUNDLE);
		triggerService.registerActionProvider(this);
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
	public void validate(TriggerAction action, Map<String, String> parameters)
			throws ValidationException {
		if(parameters.containsKey("block.ip")) {
			throw new ValidationException("IP address required");
		}
	}

	@Override
	public ActionResult execute(TriggerAction action, SystemEvent event)
			throws ValidationException {
		
		String ipAddress = event.getAttribute(CommonAttributes.ATTR_IP_ADDRESS);
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Blocking IP address "  + ipAddress);
			}
			
			InetAddress addr = InetAddress.getByName(event.getAttribute(CommonAttributes.ATTR_IP_ADDRESS));
			
			server.blockAddress(addr);
			
			if(log.isInfoEnabled()) {
				log.info("Blocked IP address " + ipAddress);
			}
			
			int val = 0;
			
			if((val = repository.getIntValue(action, "block.length")) > 0) {
				
				if(log.isInfoEnabled()) {
					log.info("Scheduling unblock for IP address " + ipAddress + " in " + val + " minutes");
				}
				
				JobDataMap data = new JobDataMap();
				data.put("addr", addr);
				
				schedulerService.scheduleIn(UnblockIPJob.class, data, val * 60000);
			}
			return new BlockedIPResult(this, event.getCurrentRealm(), action, ipAddress);
		} catch (UnknownHostException | SchedulerException e) {
			log.error("Failed to fully process block IP request for " + ipAddress, e);
			return new BlockedIPResult(this, e, event.getCurrentRealm(), action, ipAddress);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
