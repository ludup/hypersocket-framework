package com.hypersocket.ip;

import java.net.InetAddress;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.geo.IStackLocation;
import com.hypersocket.geo.IStackService;
import com.hypersocket.realm.Realm;

@Component
public class GeoIPRestrictrictionProvider implements IPRestrictionProvider {

	static Logger log = LoggerFactory.getLogger(IPRestrictionServiceImpl.class);

	@Autowired
	private SystemConfigurationService systemConfiguration;
	
	@Autowired
	private IStackService istackService;

	@Autowired
	private IPRestrictionService ipRestrictionService;


	public GeoIPRestrictrictionProvider() {
	}

	@PostConstruct
	private void setup() {
		ipRestrictionService.registerProvider(this);
	}

	public synchronized boolean isAllowedAddress(InetAddress addr, String service, Realm realm) {
		
		if(hasAPIKey()) {
			try {
				IStackLocation location = istackService.lookupGeoIP(addr.getHostAddress());
				
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
}
