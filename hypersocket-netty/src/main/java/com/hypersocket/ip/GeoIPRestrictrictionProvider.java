package com.hypersocket.ip;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.cache.CacheService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.realm.Realm;
import com.hypersocket.utils.HttpUtils;

@Component
public class GeoIPRestrictrictionProvider implements IPRestrictionProvider {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);

	@Autowired
	private SystemConfigurationService systemConfiguration;
	
	@Autowired
	private HttpUtils httpUtils;

	@Autowired
	private IPRestrictionService ipRestrictionService;

	@Autowired
	private CacheService cacheService; 

	public GeoIPRestrictrictionProvider() {
	}

	@PostConstruct
	private void setup() {
		ipRestrictionService.registerProvider(this);
	}

	public synchronized boolean isAllowedAddress(InetAddress addr, String service, Realm realm) {
		
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
}
