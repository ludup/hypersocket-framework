package com.hypersocket.template;

import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.tables.ColumnSort;

public interface TemplateRepository extends AbstractResourceRepository<Template> {

	List<Template> search(Realm realm, String searchPattern, String type,
			int start, int length, ColumnSort[] sorting);

	long getResourceCount(Realm realm, String searchPattern, String type);

}
