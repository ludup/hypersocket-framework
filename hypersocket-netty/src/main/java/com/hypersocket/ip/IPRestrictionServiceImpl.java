package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hypersocket.netty.NettyServer;
import com.hypersocket.realm.Realm;
import com.hypersocket.server.IPRestrictionConsumer;
import com.hypersocket.server.IPRestrictionProvider;
import com.hypersocket.server.IPRestrictionService;
import com.hypersocket.server.MutableIPRestrictionProvider;

@Service
public class IPRestrictionServiceImpl implements IPRestrictionService {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);

	private List<IPRestrictionProvider> providers = new ArrayList<>();
	private Map<String, IPRestrictionConsumer> services = new HashMap<>();

	private MutableIPRestrictionProvider mutableProvider;
	
	static boolean globalDisabled = "true".equals(System.getProperty("hypersocket.disableIpRestrictions", "false"));

	@Override
	public void registerProvider(IPRestrictionProvider provider) {
		providers.add(provider);
		Collections.sort(providers, (p1, p2) -> Integer.valueOf(p1.getWeight()).compareTo(p2.getWeight()) );
		mutableProvider = null;
		for(var p : providers) {
			if(mutableProvider  == null && p instanceof MutableIPRestrictionProvider) {
				mutableProvider = (MutableIPRestrictionProvider) p;
				break;
			}
		}
	}

	@Override
	public boolean isBlockedAddress(InetAddress addr, String service, Realm realm) {
		return !isAllowedAddress(addr, service, realm);
	}

	@Override
	public boolean isAllowedAddress(InetAddress addr, String service, Realm realm) {
		if(globalDisabled)
			return true;
		
		if (StringUtils.isBlank(service))
			service = DEFAULT_SERVICE;
		for (IPRestrictionProvider provider : providers) {
			if (!provider.isAllowedAddress(addr, service, realm))
				return false;
		}

		if (!DEFAULT_SERVICE.equals(service)) {
			for (IPRestrictionProvider provider : providers) {
				if (!provider.isAllowedAddress(addr, DEFAULT_SERVICE, realm))
					return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean isBlockedAddress(String addr, String service, Realm realm) throws UnknownHostException {
		return isBlockedAddress(InetAddress.getByName(addr), service, realm);
	}

	@Override
	public boolean isAllowedAddress(String ip, String service, Realm realm) throws UnknownHostException {

		InetAddress addr = InetAddress.getByName(ip);
		return isAllowedAddress(addr, service, realm);
	}

	@Override
	public void registerService(IPRestrictionConsumer service) {
		if (services.containsKey(service.getName()))
			throw new IllegalStateException(String.format("Service %s already registered.", service));
		services.put(service.getName(), service);
	}

	@Override
	public Collection<IPRestrictionConsumer> getServices() {
		return services.values();
	}

	@PostConstruct
	private void setup() {
		registerService(new IPRestrictionConsumer(NettyServer.RESOURCE_BUNDLE,
				IPRestrictionService.DEFAULT_SERVICE));
	}

	@Override
	public MutableIPRestrictionProvider getMutableProvider() {
		return mutableProvider;
	}

}
