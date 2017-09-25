package com.hypersocket.realm;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.utils.StaticResolver;

public class ServerResolver extends StaticResolver {

	public ServerResolver(Realm realm) {
		
		ConfigurationService configurationService = ApplicationContextServiceImpl.getInstance().getBean(ConfigurationService.class);
		RealmService realmService = ApplicationContextServiceImpl.getInstance().getBean(RealmService.class);
		String serverHost = configurationService.getValue(realm,"email.externalHostname");
		if(StringUtils.isBlank(serverHost)) {
			serverHost = realmService.getRealmHostname(realm);
		}
		String serverUrl = String.format("https://%s/", serverHost);
		
		addToken("serverName", configurationService.getValue(realm, "email.serverName"));
		addToken("serverUrl", serverUrl);
		addToken("serverHost", serverHost);
	}
}
