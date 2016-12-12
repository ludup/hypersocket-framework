package com.hypersocket.browser;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

public interface BrowserLaunchableService extends AuthenticatedService {

	String getFingerprint();
	
	void updateFingerprint();
	
	List<BrowserLaunchable> searchPersonalResources(Principal principal,
			String search, int start, int length, ColumnSort[] sorting);

	long getPersonalResourceCount(Principal principal, String search);

	List<BrowserLaunchable> getPersonalResources(Principal principal);

	void registerService(Class<? extends BrowserLaunchable> clz, AbstractAssignableResourceService<?> service);

	void deleteResource(Long id) throws AccessDeniedException, ResourceException;

}
