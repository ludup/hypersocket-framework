package com.hypersocket.automation;

import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tables.ColumnSort;


public interface AutomationResourceRepository extends
		AbstractResourceRepository<AutomationResource> {

	List<?> getCsvAutomations(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting);

	Long getCsvAutomationsCount(Realm realm, String searchColumn, String searchPattern);

	AutomationResource getAutomationById(Long id, Realm currentRealm);
}
