package com.hypersocket.migration.properties;

import com.hypersocket.migration.repository.MigrationCriteriaBuilder;
import com.hypersocket.repository.AbstractEntity;

import java.util.List;
import java.util.Map;

public interface MigrationProperties {
    Short sortOrder();
    List<Class<? extends AbstractEntity<Long>>>  getOrderList();
    Map<Class<?>, MigrationCriteriaBuilder> getCriteriaMap();
}
