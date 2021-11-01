package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;

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
				if (rule.matches(InetSocketAddress.createUnresolved(addr.getHostAddress(), 0))) {
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

		IpSubnetFilterRule rule = new IpSubnetFilterRule(DefaultIPRestrictionProvider.processAddressForIp(addr), DefaultIPRestrictionProvider.processAddressForCIDR(addr), IpFilterRuleType.REJECT);
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

		IpSubnetFilterRule rule = new IpSubnetFilterRule(DefaultIPRestrictionProvider.processAddressForIp(addr), DefaultIPRestrictionProvider.processAddressForCIDR(addr), IpFilterRuleType.REJECT);
		deny.remove(rule);

	}

	public static String processAddressForIp(String addr) {
		int idx = addr.indexOf('/');
		if (idx == -1) {
			return addr;
		}
		return addr.substring(0, idx);
	}


	public static int processAddressForCIDR(String addr) {
		int idx = addr.indexOf('/');
		if (idx == -1) {
			if (addr.indexOf(":") > -1) {
				return 128;
			} else {
				return 32;
			}
		}
		else
			return Integer.parseInt(addr.substring(idx + 1));
	}

	@EventListener()
	private void contextStarted(ContextStartedEvent cse) {
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
