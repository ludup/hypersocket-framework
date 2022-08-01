package com.hypersocket.plugins;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDependency;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;

public class PluginResource {

	private final PluginState state;
	private final RuntimeMode mode;
	private final String id;
	private final String description;
	private final String version;
	private final String provider;
	private final String license;
	private final List<PluginDependency> dependencies;
	private final String path;

	public PluginResource() {
		state = null;
		mode = null;
		id = null;
		description = null;
		version = null;
		provider = null;
		license = null;
		dependencies = Collections.emptyList();
		path = null;
	}

	public PluginResource(PluginWrapper p) {
		state = p.getPluginState();
		mode = p.getRuntimeMode();
		id = p.getPluginId();
		description = p.getDescriptor().getPluginDescription();
		version = p.getDescriptor().getVersion();
		provider = p.getDescriptor().getProvider();
		license = p.getDescriptor().getLicense();
		path = p.getPluginPath().toString();
		dependencies = p.getDescriptor().getDependencies();
	}

	public PluginState getState() {
		return state;
	}

	public RuntimeMode getMode() {
		return mode;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		/*
		 * INFO BPS 31/07/22 Delete action requires a name
		 */
		return getId();
	}

	public String getDescription() {
		return description;
	}

	public String getVersion() {
		return version;
	}

	public String getProvider() {
		return provider;
	}

	public String getLicense() {
		return license;
	}

	public List<PluginDependency> getDependencies() {
		return dependencies;
	}

	public String getPath() {
		return path;
	}

	public boolean matches(String searchPattern, String searchColumn) {
		if (StringUtils.isBlank(searchPattern))
			return true;
		// TODO do it proper like
		return matchesString(id, searchPattern) || matchesString(description, searchPattern)
				|| matchesString(version, searchPattern) || matchesString(provider, searchPattern)
				|| matchesString(license, searchPattern);
	}

	private boolean matchesString(String text, String pattern) {
		return StringUtils.isNotBlank(text) && text.toLowerCase().contains(pattern.toLowerCase());
	}

}
