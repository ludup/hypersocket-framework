package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.handler.ipfilter.IpFilterRule;
import org.jboss.netty.handler.ipfilter.IpSubnetFilterRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;

@Component
public class DefaultIPRestrictionProvider implements IPRestrictionProvider {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);

	private Set<IpFilterRule> deny = Collections.synchronizedSet(new HashSet<IpFilterRule>());

	@Autowired
	private SystemConfigurationService configurationService;

	@Autowired
	private IPRestrictionService ipRestrictionService;

	public DefaultIPRestrictionProvider() {
	}

	@PostConstruct
	private void setup() {
		ipRestrictionService.registerProvider(this);
	}

	public boolean isAllowedAddress(InetAddress addr, String service, Realm realm) {

		synchronized(deny) {
			for (IpFilterRule rule : deny) {
				if (rule.contains(addr)) {
					return false;
				}
			}
	
			return true;
		}
	}

	public void blockIPAddress(String addr) throws UnknownHostException {
		if (log.isInfoEnabled()) {
			log.info("Blocking " + addr);
		}

		IpSubnetFilterRule rule = new IpSubnetFilterRule(false, processAddress(addr));
		deny.add(rule);
	}

	public synchronized void blockIPAddress(String addr, boolean permanent) throws UnknownHostException {

		if (permanent) {
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

	public synchronized void unblockIPAddress(String addr) throws UnknownHostException {

		if (log.isInfoEnabled()) {
			log.info("Unblocking " + addr);
		}

		IpFilterRule rule = new IpSubnetFilterRule(false, processAddress(addr));
		deny.remove(rule);

	}

	public static String processAddress(String addr) {

		if (addr.indexOf('/') == -1) {
			if (addr.indexOf(":") > -1) {
				addr += "/128";
			} else {
				addr += "/32";
			}
		}
		return addr;
	}

	@EventListener()
	private void contextStarted(ContextStartedEvent cse) {
		loadBlockedIPs();
	}

	@EventListener
	private void configurationValueChanged(ConfigurationValueChangedEvent c) {
		if ("server.blockIPs".equals(c.getAttribute(ConfigurationValueChangedEvent.ATTR_CONFIG_RESOURCE_KEY))) {
			String[] oldValues = c.getAttribute(ConfigurationValueChangedEvent.ATTR_OLD_VALUE).split("\\r\\n");
			String[] newValues = c.getAttribute(ConfigurationValueChangedEvent.ATTR_NEW_VALUE).split("\\r\\n");

			for (String ip : newValues) {
				if (StringUtils.isBlank(ip)) {
					continue;
				}
				boolean found = false;
				for (String ip2 : oldValues) {
					found |= ip.equals(ip2);
				}

				if (!found) {
					try {
						blockIPAddress(ip);
					} catch (UnknownHostException e) {
						log.error("Failed to block new ip listed in permanent ip restrictions " + ip);
					}
				}
			}
			for (String ip : oldValues) {
				if (StringUtils.isBlank(ip)) {
					continue;
				}
				boolean found = false;
				for (String ip2 : newValues) {
					found |= ip.equals(ip2);
				}
				if (!found) {
					try {
						unblockIPAddress(ip);
					} catch (UnknownHostException e) {
						log.error("Failed to unblock ip no longer listed in permanent ip restrictions " + ip);
					}
				}
			}
		}
	}

	private void loadBlockedIPs() {

		if (log.isInfoEnabled()) {
			log.info("Loading blocked IPs");
		}

		String[] blocked = configurationService.getValues("server.blockIPs");
		for (String ip : blocked) {
			try {
				blockIPAddress(ip);
			} catch (UnknownHostException e) {
				log.info("Failed to load blocked ip " + ip, e);
			}
		}

	}
}
