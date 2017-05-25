package com.hypersocket.migration.customized;

import java.util.zip.ZipInputStream;

import com.hypersocket.migration.execution.MigrationExecutorTracker;
import com.hypersocket.realm.Realm;

public interface MigrationCustomImport<E> {

	Class<E> getType();
	void _import(String fileName, Realm realm, ZipInputStream zipInputStream, MigrationExecutorTracker migrationExecutorTracker);
}
