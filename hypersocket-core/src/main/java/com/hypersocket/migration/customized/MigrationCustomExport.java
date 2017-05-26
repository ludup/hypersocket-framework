package com.hypersocket.migration.customized;

import java.util.Set;
import java.util.zip.ZipOutputStream;

import com.hypersocket.realm.Realm;

public interface MigrationCustomExport<E> {

	boolean include(Set<String> entities);
	Class<E> getType();
	Short sortOrder();
	void export(Realm realm,  ZipOutputStream zipOutputStream);
}
