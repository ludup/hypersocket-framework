package com.hypersocket.realm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DefaultTableFilter;

@Component
public class UserNeverLoggedInFilter extends DefaultTableFilter {

	static Logger log = LoggerFactory.getLogger(UserNeverLoggedInFilter.class);
	
	@Autowired
	private UserPrincipalRepository userPrincipalRepository;
	
	@Override
	public String getResourceKey() {
		return "filter.user.never.logged.in";
	}

	@Override
	public List<?> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {
		return userPrincipalRepository.getNeverLoggedInSearch(realm, searchColumn, searchPattern, start, length, sorting);
	}

	@Override
	public Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern) {
		return userPrincipalRepository.getNeverLoggedInCount(realm, searchColumn, searchPattern);
	}

	@Override
	public List<?> searchPersonalResources(Principal principal, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long searchPersonalResourcesCount(Principal principal, String searchColumn, String searchPattern) {
		throw new UnsupportedOperationException();
	}
}
