package com.hypersocket.realm;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.utils.StaticResolver;

public class ServerResolver extends StaticResolver {

	public ServerResolver(Realm realm) {
		
		ConfigurationService configurationService = ApplicationContextServiceImpl.getInstance().getBean(ConfigurationService.class);
		RealmService realmService = ApplicationContextServiceImpl.getInstance().getBean(RealmService.class);
		String serverUrl = configurationService.getValue(realm,"email.externalHostname");
		if(StringUtils.isBlank(serverUrl)) {
			serverUrl = realmService.getRealmHostname(realm);
		}
		if(!serverUrl.startsWith("http")) {
			serverUrl = String.format("https://%s/", serverUrl);
		}
		
		addToken("serverName", configurationService.getValue(realm, "email.serverName"));
		addToken("serverUrl", serverUrl);
	}
}
