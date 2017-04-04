package com.hypersocket.migration.properties;

import com.hypersocket.repository.AbstractEntity;

import java.util.List;

public interface MigrationProperties {

    Short sortOrder();
    List<Class<? extends AbstractEntity<Long>>>  getOrderList();
}
