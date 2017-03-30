package com.hypersocket.migration.exporter;

import com.hypersocket.realm.Realm;

import java.util.List;
import java.util.Map;

public interface MigrationExporter<E> {
    Class<E> getType();
    Map<String, List<Map<String, ?>>> produceCustomOperationsMap(Realm realm);
}
