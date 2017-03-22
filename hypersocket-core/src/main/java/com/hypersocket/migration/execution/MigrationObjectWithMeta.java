package com.hypersocket.migration.execution;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.repository.AbstractEntity;

import java.util.List;

public class MigrationObjectWithMeta {

    AbstractEntity<Long> entity;
    List<DatabaseProperty>  databaseProperties;

    @SuppressWarnings("unchecked")
    public MigrationObjectWithMeta(AbstractEntity entity, List<DatabaseProperty> databaseProperties) {
        this.entity = entity;
        this.databaseProperties = databaseProperties;
    }

    public AbstractEntity<Long> getEntity() {
        return entity;
    }

    public void setEntity(AbstractEntity<Long> entity) {
        this.entity = entity;
    }

    @JsonDeserialize(using = MigrationDeserializer.class)
    public List<DatabaseProperty> getDatabaseProperties() {
        return databaseProperties;
    }

    public void setDatabaseProperties(List<DatabaseProperty> databaseProperties) {
        this.databaseProperties = databaseProperties;
    }
}
