package com.hypersocket.geo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.cache.CacheService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.session.IStackLocation;
import com.hypersocket.utils.HttpUtils;

@Component
public class IStackServiceImpl implements IStackService {

	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private SystemConfigurationService systemConfiguration;
	
	@Autowired
	private HttpUtils httpUtils;
	
	@Override
	public IStackLocation lookupGeoIP(String ipAddress) throws IOException {
		
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
