package com.hypersocket.browser;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.realm.Principal;
import com.hypersocket.tables.ColumnSort;

public interface BrowserLaunchableService extends AuthenticatedService {

	String getFingerprint();
	
	void updateFingerprint();
	
	List<BrowserLaunchable> searchPersonalResources(Principal principal,
			String search, int start, int length, ColumnSort[] sorting);

	long getPersonalResourceCount(Principal principal, String search);

	List<BrowserLaunchable> getPersonalResources(Principal principal);

}
