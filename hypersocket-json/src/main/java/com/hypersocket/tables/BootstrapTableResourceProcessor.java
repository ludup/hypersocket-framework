package com.hypersocket.tables;

import java.io.IOException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

public interface BootstrapTableResourceProcessor<T> extends BootstrapTablePageProcessor {

	T getResource() throws AccessDeniedException, IOException;
}
