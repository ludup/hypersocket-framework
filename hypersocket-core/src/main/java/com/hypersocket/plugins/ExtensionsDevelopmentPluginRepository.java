package com.hypersocket.plugins;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.util.AndFileFilter;

public class ExtensionsDevelopmentPluginRepository extends DevelopmentPluginRepository {

	public ExtensionsDevelopmentPluginRepository(Path... pluginsRoots) {
		super(pluginsRoots);
		addFilter();
	}

	public ExtensionsDevelopmentPluginRepository(List<Path> pluginsRoots) {
		super(pluginsRoots);
		addFilter();
	}

	private void addFilter() {
		var jcp = Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator));
		setFilter(new AndFileFilter(filter).addFileFilter(p -> {
			var exists = new File(p, "plugin.properties").exists();
			if (exists) {
				var cp = new File(new File(p, "target"), "classes");
				return jcp.contains(cp.getAbsolutePath());
			}
			return false;
		}));
	}

	@Override
	public boolean deletePluginPath(Path pluginPath) {
		return false;
	}

}
