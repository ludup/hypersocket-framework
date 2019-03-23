package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.handler.ipfilter.IpFilterRule;
import org.jboss.netty.handler.ipfilter.IpSubnetFilterRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

@Service
public class IPRestrictionServiceImpl implements IPRestrictionService, ApplicationListener<ApplicationEvent> {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);
	
	Set<IpFilterRule> allow = new HashSet<IpFilterRule>();
	Set<IpFilterRule> deny = new HashSet<IpFilterRule>();
	
	@Autowired
	SystemConfigurationService configurationService;

	@Override
	public synchronized void blockIPAddress(String addr) throws UnknownHostException {
		
		if(log.isInfoEnabled()) {
			log.info("Blocking " + addr);
		}
		
		IpSubnetFilterRule rule = new IpSubnetFilterRule(false, processAddress(addr));
		deny.add(rule);

	}
	
	public boolean hasAllowRule() {
		return allow.size() > 0;
	}
	
	@Override
	public synchronized void clearRules(boolean allowed, boolean blocked) {
		if(allowed) {
			allow.clear();
		}
		if(blocked) {
			deny.clear();
		}
	}
	
	@Override
	public synchronized void blockIPAddress(String addr, boolean permanent) throws UnknownHostException {
		
		if(permanent) {
			String[] oldValues = configurationService.getValues("server.blockIPs");
			try {
				configurationService.setValues("server.blockIPs", ArrayUtils.add(oldValues, addr));
			} catch (ResourceException | AccessDeniedException e) {
				throw new IllegalStateException("Failed to permanently block " + addr);
			}
		} else {
			blockIPAddress(addr);
		}
		
	}
	
	@Override
	public synchronized void unblockIPAddress(String addr) throws UnknownHostException {
		
		if(log.isInfoEnabled()) {
			log.info("Unblocking " + addr);
		}

		IpFilterRule rule = new IpSubnetFilterRule(false, processAddress(addr));
		deny.remove(rule);
		
	}
	
	private String processAddress(String addr) {

		if(addr.indexOf('/')==-1) {
			if(addr.indexOf(":") > -1) {
				addr += "/128";
			} else {
				addr += "/32";
			}
		}
		return addr;
	}

	@Override
	public synchronized void disallowIPAddress(String addr) throws UnknownHostException {
		
		if(log.isInfoEnabled()) {
			log.info("Removing allow rule for " + addr);
		}
		
		IpFilterRule rule = new IpSubnetFilterRule(true, processAddress(addr));
		allow.remove(rule);
	}

	@Override
	public synchronized void allowIPAddress(String addr) throws UnknownHostException {
		
		if(log.isInfoEnabled()) {
			log.info("Allowing " + addr);
		}
		
		IpFilterRule rule = new IpSubnetFilterRule(true, processAddress(addr));
		allow.add(rule);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		
		if(event instanceof ContextStartedEvent) {
			loadBlockedIPs();
		} else if(event instanceof ConfigurationValueChangedEvent) {
			ConfigurationValueChangedEvent c = (ConfigurationValueChangedEvent)event;
			if("server.blockIPs".equals(c.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY))) {
				String[] oldValues = c.getAttribute(ConfigurationValueChangedEvent.ATTR_OLD_VALUE).split("\\r\\n");
				String[] newValues = c.getAttribute(ConfigurationValueChangedEvent.ATTR_NEW_VALUE).split("\\r\\n");
				
				for(String ip : newValues) {
					if(StringUtils.isBlank(ip)) {
						continue;
					}
					boolean found = false;
					for(String ip2 : oldValues) {
						found |= ip.equals(ip2);
					}
					
					if(!found) {
						try {
							blockIPAddress(ip);
						} catch (UnknownHostException e) {
							log.error("Failed to block new ip listed in permanent ip restrictions " + ip);
						}
					}
				}
				for(String ip : oldValues) {
					if(StringUtils.isBlank(ip)) {
						continue;
					}
					boolean found = false;
					for(String ip2 : newValues) {
						found |= ip.equals(ip2);
					}
					if(!found) {
						try {
							unblockIPAddress(ip);
						} catch (UnknownHostException e) {
							log.error("Failed to unblock ip no longer listed in permanent ip restrictions " + ip);
						}
					}
				}
				
				
			}
		}
	}
	
	private void loadBlockedIPs() {
		
		if(log.isInfoEnabled()) {
			log.info("Loading blocked IPs");
		}
		
		String[] blocked = configurationService.getValues("server.blockIPs");
		for(String ip : blocked) {
			try {
				blockIPAddress(ip);
			} catch (UnknownHostException e) {
				log.info("Failed to load blocked ip " + ip, e);
			}
		}
		
	}
	
	@Override
	public synchronized boolean isBlockedAddress(InetAddress addr) {
		return !isAllowedAddress(addr);
	}
	
	@Override
	public synchronized boolean isAllowedAddress(InetAddress addr) {
		
		if(allow.size() > 0) {
			for(IpFilterRule rule : allow) {
				if(rule.contains(addr)) {
					return true;
				}
			}
		}
		
		for(IpFilterRule rule : deny) {
			if(rule.contains(addr)) {
				return false;
			}
		}
		
		return allow.size()==0;
	}
	
	@Override
	public synchronized boolean isBlockedAddress(String addr) throws UnknownHostException {
		return isBlockedAddress(InetAddress.getByName(addr));
	}

	@Override
	public boolean isAllowedAddress(String ip) throws UnknownHostException {
		
		InetAddress addr = InetAddress.getByName(ip);
		return isAllowedAddress(addr);
	}


 
}
