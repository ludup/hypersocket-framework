package com.hypersocket.migration.properties;

import com.hypersocket.migration.repository.MigrationExportCriteriaBuilder;
import com.hypersocket.migration.repository.MigrationLookupCriteriaBuilder;
import com.hypersocket.repository.AbstractEntity;

import java.util.List;
import java.util.Map;

public interface MigrationProperties {
    Short sortOrder();
    List<Class<? extends AbstractEntity<Long>>>  getOrderList();
    Map<Class<?>, MigrationExportCriteriaBuilder> getExportCriteriaMap();
    Map<Class<?>, MigrationLookupCriteriaBuilder> getLookupCriteriaMap();
}
