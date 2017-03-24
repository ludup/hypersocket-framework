package com.hypersocket.migration.importer;

public interface MigrationImporter<E> {
    void process(E entity);
}
