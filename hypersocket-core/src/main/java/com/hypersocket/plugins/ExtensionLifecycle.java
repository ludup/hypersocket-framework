package com.hypersocket.plugins;

import com.hypersocket.resource.ResourceException;

public interface ExtensionLifecycle {
	default void stop() {
	}

	default void uninstall(boolean deleteData) throws Exception {
	}
}