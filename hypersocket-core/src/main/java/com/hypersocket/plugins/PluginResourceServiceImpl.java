package com.hypersocket.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.output.NullOutputStream;
import org.bouncycastle.util.io.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.events.EventPropertyCollector;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.plugins.events.PluginResourceCreatedEvent;
import com.hypersocket.plugins.events.PluginResourceDeletedEvent;
import com.hypersocket.plugins.events.PluginResourceEvent;
import com.hypersocket.plugins.events.PluginResourceUpdatedEvent;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

@Service
public class PluginResourceServiceImpl extends AbstractAuthenticatedServiceImpl
		implements PluginResourceService, EventPropertyCollector {

	private final static Logger LOG = LoggerFactory.getLogger(PluginResourceServiceImpl.class);

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ExtensionsPluginManager pluginManager;

	@Autowired
	private EventService eventService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.plugins");

		for (PluginResourcePermission p : PluginResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		/**
		 * Register the events. All events have to be registerd so the system knows
		 * about them.
		 */
		eventService.registerEvent(PluginResourceEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(PluginResourceCreatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(PluginResourceUpdatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(PluginResourceDeletedEvent.class, RESOURCE_BUNDLE, this);
	}

	protected void fireResourceCreationEvent(PluginResource resource) {
		eventService.publishEvent(new PluginResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	protected void fireResourceCreationEvent(PluginResource resource, Throwable t) {
		eventService.publishEvent(new PluginResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	protected void fireResourceUpdateEvent(PluginResource resource) {
		eventService.publishEvent(new PluginResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	protected void fireResourceUpdateEvent(PluginResource resource, Throwable t) {
		eventService.publishEvent(new PluginResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	protected void fireResourceDeletionEvent(PluginResource resource, boolean deleteData) {
		eventService.publishEvent(new PluginResourceDeletedEvent(this, getCurrentSession(), resource, deleteData));
	}

	protected void fireResourceDeletionEvent(PluginResource resource, boolean deleteData, Throwable t) {
		eventService.publishEvent(new PluginResourceDeletedEvent(this, resource, t, getCurrentSession(), deleteData));
	}

	@Override
	public List<PluginResource> searchResources(Realm currentRealm, String searchColumn, String searchPattern,
			int start, int length, ColumnSort[] sorting) throws AccessDeniedException {
		List<PluginResource> l = new ArrayList<>();
		for (PluginResource r : getResources(currentRealm)) {
			if (r.matches(searchPattern, searchColumn))
				l.add(r);
		}
		Collections.sort(l, new Comparator<PluginResource>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(PluginResource o1, PluginResource o2) {
				for (ColumnSort s : sorting) {
					int i = 0;
					Comparable<?> v1 = o1.getId();
					Comparable<?> v2 = o2.getId();
					if (s.getColumn() == PluginResourceColumns.ID) {
						v1 = o1.getId();
						v2 = o1.getId();
					} else if (s.getColumn() == PluginResourceColumns.DESCRIPTION) {
						v1 = o1.getDescription();
						v2 = o2.getDescription();
					} else if (s.getColumn() == PluginResourceColumns.PROVIDER) {
						v1 = o1.getProvider();
						v2 = o2.getProvider();
					} else if (s.getColumn() == PluginResourceColumns.NAME) {
						v1 = o1.getName() == null ? null : o1.getName().toLowerCase();
						v2 = o2.getName() == null ? null : o2.getName().toLowerCase();
					} else if (s.getColumn() == PluginResourceColumns.PROVIDER) {
						v1 = o1.getProvider();
						v2 = o2.getProvider();
					} else if (s.getColumn() == PluginResourceColumns.VERSION) {
						v1 = o1.getVersion();
						v2 = o2.getVersion();
					} else if (s.getColumn() == PluginResourceColumns.STATE) {
						v1 = o1.getState();
						v2 = o2.getState();
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

	@Override
	public Long getResourceCount(Realm currentRealm, String searchColumn, String searchPattern)
			throws AccessDeniedException {
		long v = 0;
		for (PluginResource r : getResources(currentRealm)) {
			if (r.matches(searchPattern, searchColumn))
				v++;
		}
		return v;
	}

	@Override
	public Collection<PluginResource> getResources(Realm currentRealm) throws AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(PluginResourcePermission.READ);
		}
		return pluginManager.getPlugins().stream().map(p -> new PluginResource(p)).collect(Collectors.toList());
	}

	@Override
	public PluginResource getResourceById(String id) throws AccessDeniedException, IOException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(PluginResourcePermission.READ);
		}
		return getResources(getCurrentRealm()).stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
	}

	@Override
	public void deleteResource(PluginResource resource) throws AccessDeniedException, IOException {
		doDeleteResource(resource, false);
	}

	protected void doDeleteResource(PluginResource resource, boolean deleteData) throws AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(PluginResourcePermission.DELETE);
		}
		try {
			pluginManager.deletePlugin(resource.getId());
			fireResourceDeletionEvent(resource, deleteData);
		} catch (RuntimeException re) {
			fireResourceDeletionEvent(resource, deleteData, re);
			throw re;
		}
	}

	@Override
	public PluginResource disable(PluginResource resource) throws AccessDeniedException, IOException {
		try {
			if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
				assertPermission(PluginResourcePermission.UPDATE);
			}
			pluginManager.disablePlugin(resource.getId());
			fireResourceUpdateEvent(resource);
		} catch (AccessDeniedException | RuntimeException re) {
			fireResourceUpdateEvent(resource, re);
			throw re;
		}
		return getResourceById(resource.getId());
	}

	@Override
	public PluginResource enable(PluginResource resource) throws AccessDeniedException, IOException {
		try {
			if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
				assertPermission(PluginResourcePermission.UPDATE);
			}
			reload(resource);
			pluginManager.enablePlugin(resource.getId());
			fireResourceUpdateEvent(resource);
		} catch (AccessDeniedException | RuntimeException re) {
			fireResourceUpdateEvent(resource, re);
			throw re;
		}
		return getResourceById(resource.getId());
	}

	@Override
	public PluginResource stop(PluginResource resource) throws AccessDeniedException, IOException {
		try {
			if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
				assertPermission(PluginResourcePermission.UPDATE);
			}
			pluginManager.stopPlugin(resource.getId());
			reload(resource);
			fireResourceUpdateEvent(resource);
		} catch (AccessDeniedException | RuntimeException re) {
			fireResourceUpdateEvent(resource, re);
			throw re;
		}
		return getResourceById(resource.getId());
	}

	protected void reload(PluginResource resource) {
		pluginManager.unloadPlugin(resource.getId());
		pluginManager.loadPlugin(Paths.get(resource.getPath()));
	}

	@Override
	public PluginResource start(PluginResource resource) throws AccessDeniedException, IOException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(PluginResourcePermission.UPDATE);
		}
		pluginManager.startPlugin(resource.getId());

		var ext = (ExtensionPlugin) pluginManager.getPlugin(resource.getId()).getPlugin();
		ext.getWebApplicationContext(); // will initialise two new contexts, one for web and one for service

		return getResourceById(resource.getId());
	}

	@Override
	public PluginResource upload(Realm realm, InputStream inputStream, boolean start)
			throws IOException, AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(PluginResourcePermission.CREATE);
		}
		var tmpName = String.valueOf(System.currentTimeMillis()) + ".tmp";
		var tmpPluginPath = pluginManager.getPrimaryPluginsFolder().resolve(tmpName);
		String id = null;
		try {
			LOG.info("Temporarily downloading to {}", tmpPluginPath);
			var p = new Properties();
			try (var out = Files.newOutputStream(tmpPluginPath)) {
				var zfile = new ZipInputStream(new TeeInputStream(inputStream, out));
				ZipEntry zentry = null;
				while ((zentry = zfile.getNextEntry()) != null) {
					if (!zentry.isDirectory()) {
						if (zentry.getName().equals("plugin.properties")) {
							p.load(zfile);
							id = p.getProperty("plugin.id", "unknown");
							LOG.info("Found plugin meta-data for {}", id);
							p.forEach((k, v) -> LOG.info("    {}={}", k, v));
						} else
							zfile.transferTo(new NullOutputStream());
						zfile.closeEntry();
					}
				}

				inputStream.transferTo(out);
			}
			if (id == null) {
				throw new IOException("Archive contains no plugin meta-data.");
			} else {
				var realPluginPath = pluginManager.getPrimaryPluginsFolder()
						.resolve(id + "-" + p.getProperty("plugin.version", "0.0.0-SNAPSHOT") + ".zip");
				LOG.info("Moving plugin {} to {}", tmpPluginPath, realPluginPath);
				if (Files.exists(realPluginPath))
					throw new IOException(String.format("Plugin %s already exists.", id));
				Files.move(tmpPluginPath, realPluginPath);
				LOG.info("Loading plugin from {}", realPluginPath);
				pluginManager.loadPlugin(realPluginPath);
				if (start) {
					pluginManager.startPlugin(id);
				}
			}
		} finally {
			if (Files.exists(tmpPluginPath)) {
				Files.walk(tmpPluginPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}
		return getResourceById(id);
	}

	@Override
	public List<PluginResource> getResourcesByIds(String[] ids) throws AccessDeniedException, IOException {
		List<PluginResource> s = new ArrayList<>(ids.length);
		for (String i : ids)
			s.add(getResourceById(i));
		return s;
	}

	@Override
	public void deleteResources(List<PluginResource> resources) throws AccessDeniedException, IOException {
		for (PluginResource r : resources)
			deleteResource(r);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(PluginResource resource) {
		Collection<PropertyCategory> defs = getPropertyTemplate();
		for (PropertyCategory cat : defs)
			for (AbstractPropertyTemplate t : cat.getTemplates())
				((PluginPropertyTemplate) t).setResource(resource);
		return defs;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() {
		PropertyCategory pc = new PropertyCategory();
		pc.setBundle(RESOURCE_BUNDLE);
		pc.setCategoryKey("plugin");
		pc.setWeight(100);

		PluginPropertyTemplate id = new PluginPropertyTemplate();
		id.setResourceKey("id");
		id.setWeight(100);
		id.setHidden(true);
		id.getAttributes().put("inputType", "text");
		pc.getTemplates().add(id);

		PluginPropertyTemplate description = new PluginPropertyTemplate();
		description.setResourceKey("description");
		description.setWeight(110);
		description.getAttributes().put("inputType", "text");
		pc.getTemplates().add(description);

		PluginPropertyTemplate type = new PluginPropertyTemplate();
		type.setResourceKey("state");
		type.setWeight(120);
		type.getAttributes().put("inputType", "select");
		type.getAttributes().put("allowEmpty", "false");
		type.getAttributes().put("nameAttr", "name");
		type.getAttributes().put("valueAttr", "name");
		type.getAttributes().put("url", "enum/org.pf4j.PluginState/");
		type.getAttributes().put("defaultValue", "STOPPED");
		pc.getTemplates().add(type);

		PluginPropertyTemplate provider = new PluginPropertyTemplate();
		provider.setResourceKey("provider");
		provider.setWeight(130);
		provider.getAttributes().put("inputType", "text");
		pc.getTemplates().add(provider);

		PluginPropertyTemplate license = new PluginPropertyTemplate();
		license.setResourceKey("license");
		license.setWeight(140);
		license.getAttributes().put("inputType", "text");
		pc.getTemplates().add(license);

		PluginPropertyTemplate version = new PluginPropertyTemplate();
		version.setResourceKey("version");
		version.setWeight(150);
		version.getAttributes().put("inputType", "text");
		pc.getTemplates().add(version);

		PluginPropertyTemplate path = new PluginPropertyTemplate();
		path.setResourceKey("path");
		path.setWeight(160);
		path.getAttributes().put("inputType", "text");
		pc.getTemplates().add(path);

		return Arrays.asList(pc);
	}

	static class PluginPropertyTemplate extends AbstractPropertyTemplate {
		private PluginResource resource;

		@Override
		public String getValue() {
			if (resource != null) {
				if (getResourceKey().equals("id"))
					return resource.getId();
				if (getResourceKey().equals("description"))
					return resource.getDescription();
				if (getResourceKey().equals("name"))
					return resource.getName();
				if (getResourceKey().equals("license"))
					return resource.getLicense();
				if (getResourceKey().equals("provider"))
					return resource.getProvider();
				if (getResourceKey().equals("version"))
					return resource.getVersion();
				if (getResourceKey().equals("state"))
					return resource.getState().name();
			}
			return null;
		}

		public void setResource(PluginResource resource) {
			this.resource = resource;
		}

	}

	@Override
	public void uninstall(PluginResource resource, boolean deleteData) throws Exception {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(PluginResourcePermission.DELETE);
		}

		var ext = (ExtensionPlugin) pluginManager.getPlugin(resource.getId()).getPlugin();
		try {
			ext.uninstall(deleteData);
		} catch (Exception e) {
			throw e;
		} finally {
			doDeleteResource(resource, deleteData);
		}
	}

	public String getName() {
		return getClass().getName();
	}

	@Override
	public PluginResource updateResource(Realm realm, PluginResource resourceById, Map<String, String> properties)
			throws AccessDeniedException, IOException {
		if (resourceById != null)
			deleteResource(resourceById);
		createResource(realm, properties);
		return null;
	}

	@Override
	public PluginResource createResource(Realm realm, Map<String, String> properties)
			throws IOException, AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		PluginResource ipr = new PluginResource();
//		ipr.setDestination(properties.get("destination"));
//		ipr.setGateway(properties.get("gateway"));
//		ipr.setIface(properties.get("iface"));
//		try {
//			ipr.setPrefix(Integer.parseInt(properties.get("prefix")));
//		} catch (NumberFormatException | NullPointerException nfe) {
//			//
//		}
//		ipr.setType(RouteType.valueOf(properties.get("type")));
//		ipr.setProvider(RouteProvider.USER);
//		ipr.create();
		return ipr;
	}

	@Override
	public Set<String> getPropertyNames(String resourceKey, Realm realm) {
		// TODO Auto-generated method stub
		return null;
	}

}
