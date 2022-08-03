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
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.CoreStartedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.server.IPRestrictionService;
import com.hypersocket.server.MutableIPRestrictionProvider;

@Component
public class DefaultIPRestrictionProvider implements MutableIPRestrictionProvider {

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

	@Override
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

	@Override
	public synchronized void denyIPAddress(Realm realm, String addr, boolean permanent) throws UnknownHostException {

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

	@Override
	public synchronized void undenyIPAddress(Realm realm, String addr) throws UnknownHostException {

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
	private void contextStarted(CoreStartedEvent cse) {
		loadBlockedIPs();
	}

	@EventListener
	private void configurationValueChanged(ConfigurationValueChangedEvent c) {
		if ("server.blockIPs".equals(c.getConfigResourceKey())) {
			String[] oldValues = c.getOldValue().split("\\r\\n");
			String[] newValues = c.getNewValue().split("\\r\\n");

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
						undenyIPAddress(c.getCurrentRealm(), ip);
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

	@Override
	public void clearRules(Realm realm, boolean allow, boolean block) {
		if(block) {
			try {
				configurationService.setValues("server.blockIPs", new String[0]);
			} catch (ResourceException | AccessDeniedException e) {
				throw new IllegalStateException("Failed to clear rules.", e);
			}
		}
		if(allow)
			throw new UnsupportedOperationException("This IP restriction provider does not support allow rules. Please try the Enhanced Security extension.");
	}

	@Override
	public void allowIPAddress(Realm realm, String ipAddress, boolean permanent) throws UnknownHostException {
		throw new UnsupportedOperationException("This IP restriction provider does not support allow rules. Please try the Enhanced Security extension.");
	}

	@Override
	public void disallowIPAddress(Realm realm, String ipAddress, boolean permanent) throws UnknownHostException {
		throw new UnsupportedOperationException("This IP restriction provider does not support allow rules. Please try the Enhanced Security extension.");
	}

	@Override
	public int getWeight() {
		return 1000;
	}
}
