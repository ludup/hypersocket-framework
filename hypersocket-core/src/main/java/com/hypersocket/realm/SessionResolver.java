package com.hypersocket.realm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hypersocket.session.Session;
import com.hypersocket.utils.HypersocketUtils;

public class SessionResolver extends PrincipalWithoutPasswordResolver {

	
	public SessionResolver(Session session) {
		super((UserPrincipal)session.getCurrentPrincipal());
		addToken("sessionId", session.getId());
		addToken("sessionCreated", HypersocketUtils.formatDateTime(session.getCreateDate()));
		addToken("sessionOs", session.getOs());
		addToken("sessionOsVersion", session.getOsVersion());
		addToken("sessionRemoteAddress", session.getRemoteAddress());
		addToken("sessionUserAgent", session.getUserAgent());
		addToken("sessionUserAgentVersion", session.getUserAgentVersion());
	}
	
	public static Set<String> getVariables() {
		Set<String> ret = new HashSet<String>();
		ret.addAll(PrincipalWithoutPasswordResolver.getVariables());
		ret.addAll(Arrays.asList("sessionId", "sessionCreated", "sessionOs", "sessionOsVersion",
				"sessionRemoteAddress", "sessionUserAgent", "sessionUserAgentVersion"));
		return ret;
	}
}