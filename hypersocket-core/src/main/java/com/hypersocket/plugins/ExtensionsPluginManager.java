package com.hypersocket.plugins;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginLoader;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginLoader;
import org.pf4j.PluginRepository;
import org.pf4j.PropertiesPluginDescriptorFinder;
import org.pf4j.RuntimeMode;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.hypersocket.json.version.HypersocketVersion;

public final class ExtensionsPluginManager extends SpringPluginManager {
	private final static Logger LOG = LoggerFactory.getLogger(ExtensionsPluginManager.class);
	
	private ApplicationContext webApplicationContext;
	private ServletConfig servletConfig;
	private ServletContext servletContext;
	
	{
		setSystemVersion(ExtensionsPluginManager.stripSnapshot(HypersocketVersion.getVersion()));
	}
	
	public void startWebContext(ApplicationContext context, ServletConfig servletConfig, ServletContext servletContext) {
		this.webApplicationContext = context;
		this.servletContext = servletContext;
        var startedPlugins = getStartedPlugins();
        for (var plugin : startedPlugins) {
        	var pluginInstance = (ExtensionPlugin)plugin.getPlugin();
        	
        	/* First call initialises it */
        	pluginInstance.getWebApplicationContext();
        }
	}
	
	public ServletConfig getServletConfig() {
		return servletConfig;
	}
	
	@Override
	protected PluginDescriptorFinder createPluginDescriptorFinder() {
		var f = new CompoundPluginDescriptorFinder();
		if(isDevelopment())
			f.add(new ExtensionsDevelopmentPluginDescriptorFinder());
		f.add(new PropertiesPluginDescriptorFinder());
		return f;
	}

	@Override
	protected PluginLoader createPluginLoader() {
		return new CompoundPluginLoader()
				.add(new DefaultPluginLoader(this))
				.add(new DevelopmentPluginLoader(this), this::isDevelopment);
	}

	@Override
	protected PluginRepository createPluginRepository() {
		return new CompoundPluginRepository()
				.add(new ExtensionsDevelopmentPluginRepository(getDevelopmentPluginsRoots()), this::isDevelopment)
				.add(new DefaultPluginRepository(getPluginsRoots()));
	}

	@Override
	public RuntimeMode getRuntimeMode() {
		/* Be compatible with our existing hypersocket.development flag */
		if (runtimeMode == null) {
			runtimeMode = Boolean.getBoolean("hypersocket.development") ? RuntimeMode.DEVELOPMENT
					: RuntimeMode.DEPLOYMENT;
		}
		return runtimeMode;
	}

	@Override
	protected List<Path> createPluginsRoot() {
		var roots = new ArrayList<Path>();

		var hypersocketConf = System.getProperty("hypersocket.conf", "conf");
		var confDir = Paths.get(hypersocketConf);

		/* User plugins */
		roots.add(confDir.resolve("plugins"));
		
		LOG.info("Plugin roots: ");
		for(var root: roots) {
			LOG.info("   {}", root);
		}
		
		return roots;
	}

	public ApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}
	
	protected List<Path> getDevelopmentPluginsRoots() {
		var roots = new ArrayList<Path>();
		if (getRuntimeMode() == RuntimeMode.DEVELOPMENT) {
			String hypersocketEnterpriseRoot = System.getProperty("hypersocket.enterpriseRoot");
			if (StringUtils.isBlank(hypersocketEnterpriseRoot))
				throw new IllegalStateException(
						"The system property hypersocket.enterpriseRoot must be set and pointed at the root of your hypersocket.enteprise module.");

			var hypersocketEnterpriseRootPath = Paths.get(hypersocketEnterpriseRoot);
			if (!Files.exists(hypersocketEnterpriseRootPath)) {
				throw new IllegalStateException("The path " + hypersocketEnterpriseRoot
						+ " that system property hypersocket.enterpriseRoot points to must exist.");
			}
			

			roots.add(hypersocketEnterpriseRootPath);
		} 
		return roots;
	}

	static String stripSnapshot(String version) {
		int idx = version.indexOf("-");
		if (idx == -1)
			return version;
		else
			return version.substring(0, idx);
	}
}