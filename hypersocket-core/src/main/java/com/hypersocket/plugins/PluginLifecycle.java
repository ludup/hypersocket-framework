package com.hypersocket.plugins;

public interface PluginLifecycle {
	default void start() {
	}
	
	default void stop() {
	}

	default void uninstall(boolean deleteData) throws Exception {
	}
}