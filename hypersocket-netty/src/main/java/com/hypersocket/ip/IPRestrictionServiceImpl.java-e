package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.netty.handler.ipfilter.IpFilterRule;
import org.jboss.netty.handler.ipfilter.IpSubnetFilterRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationChangedEvent;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.resource.ResourceChangeException;
import com.mysql.jdbc.StringUtils;

@Service
public class IPRestrictionServiceImpl implements IPRestrictionService, ApplicationListener<ApplicationEvent> {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);
	
	List<IPRestrictionListener> listeners = new ArrayList<IPRestrictionListener>();
	
	@Autowired
	SystemConfigurationService configurationService;
	
	Set<IpFilterRule> ipRules = new HashSet<IpFilterRule>();
	

	@Override
	public void registerListener(IPRestrictionListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public synchronized void blockIPAddress(String addr) throws UnknownHostException {
		
		if(log.isInfoEnabled()) {
			log.info("Blocking " + addr);
		}
		
		String cidr = addr;
		if(cidr.indexOf('/')==-1) {
			cidr += "/32";
		}
		
		IpFilterRule rule = new IpSubnetFilterRule(false, cidr);
		ipRules.add(rule);
		
		for(IPRestrictionListener listener : listeners) {
			listener.onBlockIP(addr);
		}
	}
	
	@Override
	public synchronized void blockIPAddress(String addr, boolean permanent) throws UnknownHostException {
		
		if(permanent) {
			String[] oldValues = configurationService.getValues("server.blockIPs");
			try {
				configurationService.setValues("server.blockIPs", ArrayUtils.add(oldValues, addr));
			} catch (ResourceChangeException | AccessDeniedException e) {
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
		
		String cidr = addr;
		if(cidr.indexOf('/')==-1) {
			cidr += "/32";
		}
		
		IpFilterRule rule = new IpSubnetFilterRule(false, cidr);
		ipRules.remove(rule);
		
		for(IPRestrictionListener listener : listeners) {
			listener.onUnblockIP(addr);
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		
		if(event instanceof ContextStartedEvent) {
			loadBlockedIPs();
		} else if(event instanceof ConfigurationChangedEvent) {
			ConfigurationChangedEvent c = (ConfigurationChangedEvent)event;
			if(c.getAttribute(ConfigurationChangedEvent.ATTR_CONFIG_RESOURCE_KEY).equals("server.blockIPs")) {
				String[] oldValues = c.getAttribute(ConfigurationChangedEvent.ATTR_OLD_VALUE).split("\\r\\n");
				String[] newValues = c.getAttribute(ConfigurationChangedEvent.ATTR_NEW_VALUE).split("\\r\\n");
				
				for(String ip : newValues) {
					if(StringUtils.isEmptyOrWhitespaceOnly(ip)) {
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
					if(StringUtils.isEmptyOrWhitespaceOnly(ip)) {
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
				for(IPRestrictionListener listener : listeners) {
					listener.onBlockIP(ip);
				}
			} catch (UnknownHostException e) {
				log.info("Failed to load blocked ip " + ip, e);
			}
		}
		
	}
	
	@Override
	public synchronized boolean isBlockedAddress(InetAddress addr) {
		for(IpFilterRule rule : ipRules) {
			if(rule.contains(addr)) {
				return true;
			}
		} 
		return false;
	}
	

	@Override
	public synchronized boolean isBlockedAddress(String addr) throws UnknownHostException {
		return isBlockedAddress(InetAddress.getByName(addr));
	}


 
}
