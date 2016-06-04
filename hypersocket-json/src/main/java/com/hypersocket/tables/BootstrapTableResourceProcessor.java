package com.hypersocket.tables;

import java.io.FileNotFoundException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

public interface BootstrapTableResourceProcessor<T> extends BootstrapTablePageProcessor {

	T getResource() throws FileNotFoundException, AccessDeniedException;
}
