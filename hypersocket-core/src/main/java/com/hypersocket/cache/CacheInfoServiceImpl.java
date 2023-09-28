package com.hypersocket.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.cache.CacheManager;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.cache.ICache;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Service
public class CacheInfoServiceImpl extends AbstractAuthenticatedServiceImpl implements CacheInfoService {

	class CacheRegistration {
		ICache<?,?> cache;
		Class<?> keyClass;
		String name;
		Class<?> valueClass;
		
		CacheRegistration(String name, Class<?> keyClass, Class<?> valueClass, ICache<?,?> cache) {
			super();
			this.name = name;
			this.keyClass = keyClass;
			this.valueClass = valueClass;
			this.cache = cache;
		}
	}
	
	static class CachePropertyTemplate extends AbstractPropertyTemplate {
		private CacheInfo resource;

		@Override
		public String getValue() {
			if (resource != null) {
				if (getResourceKey().equals("id"))
					return resource.getId();
				if (getResourceKey().equals("hits"))
					return String.valueOf(resource.getHits());
				if (getResourceKey().equals("hitPercentage"))
					return String.valueOf(resource.getHitPercentage());
				if (getResourceKey().equals("misses"))
					return String.valueOf(resource.getMisses());
				if (getResourceKey().equals("missPercentage"))
					return String.valueOf(resource.getMissPercentage());
				if (getResourceKey().equals("size"))
					return String.valueOf(resource.getSize());
				// TODO more
			}
			return null;
		}

		public void setResource(CacheInfo resource) {
			this.resource = resource;
		}

	}

	static Logger log = LoggerFactory.getLogger(CacheInfoServiceImpl.class);
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;
	
	@Override
	public void deleteResource(CacheInfo resource) throws AccessDeniedException, IOException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		cacheService.getCaches().get(resource.getId()).getCache().clear();
	}

	@Override
	public void deleteResources(List<CacheInfo> resources) throws AccessDeniedException, IOException {
		for (CacheInfo r : resources)
			deleteResource(r);
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheService.getCacheManager();
	}

	public String getName() {
		return getClass().getName();
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() {
		PropertyCategory pc = new PropertyCategory();
		pc.setBundle(RESOURCE_BUNDLE);
		pc.setCategoryKey("cache");
		pc.setWeight(100);

		CachePropertyTemplate id = new CachePropertyTemplate();
		id.setResourceKey("id");
		id.setWeight(100);
		id.setHidden(true);
		id.getAttributes().put("inputType", "text");
		pc.getTemplates().add(id);

		CachePropertyTemplate hits = new CachePropertyTemplate();
		hits.setResourceKey("hits");
		hits.setWeight(110);
		hits.getAttributes().put("inputType", "text");
		pc.getTemplates().add(hits);

		CachePropertyTemplate hitPercentage = new CachePropertyTemplate();
		hitPercentage.setResourceKey("hitPercentage");
		hitPercentage.setWeight(120);
		hitPercentage.getAttributes().put("inputType", "text");
		pc.getTemplates().add(hitPercentage);

		CachePropertyTemplate misses = new CachePropertyTemplate();
		misses.setResourceKey("misses");
		misses.setWeight(130);
		misses.getAttributes().put("inputType", "text");
		pc.getTemplates().add(misses);

		CachePropertyTemplate missPercentage = new CachePropertyTemplate();
		missPercentage.setResourceKey("missPercentage");
		missPercentage.setWeight(140);
		missPercentage.getAttributes().put("inputType", "text");
		pc.getTemplates().add(missPercentage);

		CachePropertyTemplate size = new CachePropertyTemplate();
		size.setResourceKey("size");
		size.setWeight(140);
		size.getAttributes().put("inputType", "text");
		pc.getTemplates().add(size);

		return Arrays.asList(pc);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(CacheInfo resource) {
		Collection<PropertyCategory> defs = getPropertyTemplate();
		for (PropertyCategory cat : defs)
			for (AbstractPropertyTemplate t : cat.getTemplates())
				((CachePropertyTemplate) t).setResource(resource);
		return defs;
	}
	
	
	@Override
	public CacheInfo getResourceById(String id) throws AccessDeniedException, IOException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		for (CacheInfo r : getResources(getCurrentRealm())) {
			if (r.getId().equals(id))
				return r;
		}
		return null;
	}
	
	@Override
	public Long getResourceCount(Realm currentRealm, String searchColumn, String searchPattern)
			throws AccessDeniedException {
		long v = 0;
		try {
			for (CacheInfo r : getResources(currentRealm)) {
				if (r.matches(searchPattern, searchColumn))
					v++;
			}
		} catch (IOException se) {
			throw new IllegalStateException("Failed to search.", se);
		}
		return v;
	} 
	
	@Override
	public Collection<CacheInfo> getResources(Realm currentRealm) throws IOException, AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		var r = new ArrayList<CacheInfo>();
		for (var en : cacheService.getCaches().entrySet()) {
			r.add(new CacheInfo(en.getValue().getCache()));
		}
		return r;
	}

	@Override
	public List<CacheInfo> getResourcesByIds(String[] ids) throws AccessDeniedException, IOException {
		List<CacheInfo> s = new ArrayList<>(ids.length);
		for (String i : ids)
			s.add(getResourceById(i));
		return s;
	}

	@Override
	public List<CacheInfo> searchResources(Realm currentRealm, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting) throws AccessDeniedException, IOException {
		List<CacheInfo> l = new ArrayList<>();
		for (CacheInfo r : getResources(currentRealm)) {
			if (r.matches(searchPattern, searchColumn))
				l.add(r);
		}
		Collections.sort(l, new Comparator<CacheInfo>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(CacheInfo o1, CacheInfo o2) {
				for (ColumnSort s : sorting) {
					int i = 0;
					Comparable<?> v1 = o1.getId();
					Comparable<?> v2 = o2.getId();
					if (s.getColumn() == CacheInfoColumns.ID) {
						v1 = o1.getId();
						v2 = o2.getId();
					} else if (s.getColumn() == CacheInfoColumns.HITS) {
						v1 = o1.getHits();
						v2 = o2.getHits();
					} else if (s.getColumn() == CacheInfoColumns.HIT_PERCENT) {
						v1 = o1.getHitPercentage();
						v2 = o2.getHitPercentage();
					} else if (s.getColumn() == CacheInfoColumns.MISSES) {
						v1 = o1.getMisses();
						v2 = o2.getMisses();
					} else if (s.getColumn() == CacheInfoColumns.MISS_PERCENT) {
						v1 = o1.getHitPercentage();
						v2 = o2.getHitPercentage();
					} else if (s.getColumn() == CacheInfoColumns.SIZE) {
						v1 = o1.getSize();
						v2 = o2.getSize();
					}
					if (v1 == null && v2 != null)
						i = -1;
					else if (v2 == null && v1 != null)
						i = 1;
					else if (v2 != null && v1 != null) {
						i = (((Comparable<Object>) v1).compareTo((Comparable<Object>) v2));
					}
					if (i != 0) {
						return s.getSort() == Sort.ASC ? i * -1 : i;
					}
				}
				return 0;
			}
		});
		return l.subList(Math.min(l.size(), start), Math.min(l.size(), start + length));
	}

	@PostConstruct
	private void postConstruct() throws SchedulerException {
		i18nService.registerBundle(RESOURCE_BUNDLE);

	}
}
