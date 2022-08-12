package com.hypersocket.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;

public class ExtensionsDevelopmentPluginDescriptorFinder implements PluginDescriptorFinder {

	@Override
	public boolean isApplicable(Path pluginPath) {
		return Files.exists(pluginPath.resolve("pom.xml")) && Files.exists(resolvePropertiesPath(pluginPath));
	}

	@Override
	public PluginDescriptor find(Path pluginPath) {
		try(var in = Files.newBufferedReader(resolvePropertiesPath(pluginPath))) {
			var properties = new Properties();
			properties.load(in);
			return new DefaultPluginDescriptor(properties.getProperty("plugin.id"), 
					properties.getProperty("plugin.description"), properties.getProperty("plugin.class"), properties.getProperty("plugin.version"), 
					properties.getProperty("plugin.dependencies"), properties.getProperty("plugin.provide"), properties.getProperty("plugin.license"));
		}
		catch(IOException ioe) {
			throw new IllegalArgumentException("Failed to load descriptor from path.");
		}
	}

	protected Path resolvePropertiesPath(Path pluginPath) {
		return pluginPath.resolve("plugin.properties");
	}
	
}