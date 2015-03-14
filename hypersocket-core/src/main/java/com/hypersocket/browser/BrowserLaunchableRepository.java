package com.hypersocket.browser;

import java.util.List;

import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractRepository;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.tables.ColumnSort;

public interface BrowserLaunchableRepository extends AbstractRepository<Long> {

	List<BrowserLaunchable> searchAssignedResources(Principal principal,
			String searchPattern, int start, int length, ColumnSort[] sorting,
			CriteriaConfiguration... configs);

	Long getAssignedResourceCount(Principal principal, String searchPattern,
			CriteriaConfiguration... configs);

	List<BrowserLaunchable> getPersonalResources(Principal principal);

}
