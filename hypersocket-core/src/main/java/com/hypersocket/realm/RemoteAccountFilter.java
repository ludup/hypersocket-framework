package com.hypersocket.realm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DefaultTableFilter;

@Component
class RemoteAccountFilter extends DefaultTableFilter {

	@Autowired
	private RealmService realmService; 
	
	@Override
	public String getResourceKey() {
		return "filter.accounts.remote";
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {
		RealmProvider remote = realmService.getProviderForRealm(realm);
		return remote.getPrincipals(realm, PrincipalType.USER, searchColumn, searchPattern, start, length, sorting);
	}

	@Override
	public Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern) {
		RealmProvider remote = realmService.getProviderForRealm(realm);
		return remote.getPrincipalCount(realm, PrincipalType.USER, searchColumn, searchPattern);
	}

	@Override
	public List<?> searchPersonalResources(Principal principal, String searchColumn, String searchPattern,
			int start, int length, ColumnSort[] sorting) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long searchPersonalResourcesCount(Principal principal, String searchColumn, String searchPattern) {
		throw new UnsupportedOperationException();
	}

}