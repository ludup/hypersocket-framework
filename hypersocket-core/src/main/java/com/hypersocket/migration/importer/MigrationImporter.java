package com.hypersocket.migration.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.realm.Realm;

public interface MigrationImporter<E> {
    void process(E entity);
    Class<E> getType();
    void processCustomOperationsMap(JsonNode jsonNode, Realm realm);
}
