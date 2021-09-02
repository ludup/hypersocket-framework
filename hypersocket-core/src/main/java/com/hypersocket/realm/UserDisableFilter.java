package com.hypersocket.realm;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DefaultTableFilter;

@Component
public class UserDisableFilter extends DefaultTableFilter {
	
	static Logger log = LoggerFactory.getLogger(UserDisableFilter.class);
	
	@Autowired
	private PrincipalRepository principalRepository;
	
	@Override
	public String getResourceKey() {
		return "filter.ad.principal.disabled";
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {
		return principalRepository.searchRemoteUserWithPrincipalStatus(Principal.class, realm, PrincipalType.USER, 
				searchColumn, searchPattern, sorting, start, length,  Arrays.asList(PrincipalStatus.DISABLED));
	}

	@Override
	public Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern) {
		return principalRepository.getRemoteUserWithPrincipalStatusCount(Principal.class, realm, PrincipalType.USER, 
				searchColumn, searchPattern, Arrays.asList(PrincipalStatus.DISABLED));
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
