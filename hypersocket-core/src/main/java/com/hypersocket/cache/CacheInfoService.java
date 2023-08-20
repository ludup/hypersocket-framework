package com.hypersocket.cache;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.cache.CacheManager;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface CacheInfoService {

	public static final String RESOURCE_BUNDLE = "CacheInfoService";

	List<CacheInfo> searchResources(Realm currentRealm, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException, IOException;

	Long getResourceCount(Realm currentRealm, String searchColumn, String searchPattern) throws AccessDeniedException;

	Collection<CacheInfo> getResources(Realm currentRealm) throws AccessDeniedException, IOException;

	CacheInfo getResourceById(String id) throws AccessDeniedException, IOException;

	void deleteResource(CacheInfo resource) throws AccessDeniedException, IOException;

	List<CacheInfo> getResourcesByIds(String[] ids) throws AccessDeniedException, IOException;

	void deleteResources(List<CacheInfo> messageResources) throws AccessDeniedException, IOException;

	Collection<PropertyCategory> getPropertyTemplate(CacheInfo resource);

	Collection<PropertyCategory> getPropertyTemplate();

	CacheManager getCacheManager();


}
