package com.hypersocket.ip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.cache.CacheService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.netty.NettyServer;
import com.hypersocket.realm.Realm;
import com.hypersocket.utils.HttpUtils;

@Service
public class IPRestrictionServiceImpl implements IPRestrictionService {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);

	private List<IPRestrictionProvider> providers = new ArrayList<>();
	private Map<String, IPRestrictionConsumer> services = new HashMap<>();

	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private HttpUtils httpUtils;
	
	@Autowired
	private SystemConfigurationService systemConfiguration;
	
	static boolean globalDisabled = "true".equals(System.getProperty("hypersocket.disableIpRestrictions", "false"));

	@Override
	public void registerProvider(IPRestrictionProvider provider) {
		providers.add(provider);
	}

	@Override
	public synchronized boolean isBlockedAddress(InetAddress addr, String service, Realm realm) {
		return !isAllowedAddress(addr, service, realm);
	}

	@Override
	public synchronized boolean isAllowedAddress(InetAddress addr, String service, Realm realm) {
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
		
		if(hasAPIKey()) {
			try {
				IStackLocation location = lookupGeoIP(addr.getHostAddress());
				
				if(Objects.nonNull(location.getCountry_code())) {
					
					if(log.isDebugEnabled()) {
						log.debug("Authorizing geolocation for {} from {}",addr.getHostAddress(), location.getCountry_code());
					}
					String[] blocked = systemConfiguration.getValues("server.blockedCountries");
					String[] allowed = systemConfiguration.getValues("server.allowedCountries");
					
					if(allowed.length > 0) {
						// Only countries in allowed will be let in
						for(String code : allowed) {
							if(location.getCountry_code().equalsIgnoreCase(code)) {
								if(log.isDebugEnabled()) {
									log.debug("Allowing access to {} due to allowed geolocaiton {}", addr.getHostAddress(), code);
								}
								return true;
							}
						}
					}
					
					for(String code : blocked) {
						if(location.getCountry_code().equalsIgnoreCase(code)) {
							if(log.isDebugEnabled()) {
								log.debug("Denying access to {} due to geolocaiton restriction on {}", addr.getHostAddress(), code);
							}
							return false;
						}
					}
				}
			} catch (Throwable e) {
				log.error("Failed to lookup geolocation for {}", addr.getHostAddress(), e);
			}
		}

		return true;
	}

	private boolean hasAPIKey() {
		return StringUtils.isNotBlank(systemConfiguration.getValue("ipstack.accesskey"));
	}
	
	private IStackLocation lookupGeoIP(String ipAddress) throws IOException {
		
		Cache<String,IStackLocation> cached = cacheService.getCacheOrCreate(
				"geoIPs", String.class, IStackLocation.class,  
				CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY));
		
		IStackLocation loc = cached.get(ipAddress);
		if(Objects.nonNull(loc)) {
			return loc;
		}
		ObjectMapper o = new ObjectMapper();
		String accessKey = systemConfiguration.getValue("ipstack.accesskey");
		
		//06c4553e8c8f2216596a7c26d9e281a9
		
		String locationJson = httpUtils.doHttpGetContent(
				String.format("http://api.ipstack.com/%s?access_key=%s", 
							ipAddress, accessKey),
						false, 
						new HashMap<String,String>());
		
		IStackLocation location =  o.readValue(locationJson, IStackLocation.class);
		cached.put(ipAddress, location);
		return location;
		
	}
	@Override
	public synchronized boolean isBlockedAddress(String addr, String service, Realm realm) throws UnknownHostException {
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

}
