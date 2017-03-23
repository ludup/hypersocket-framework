package com.hypersocket.migration.order;

import com.hypersocket.repository.AbstractEntity;

import java.util.List;

public interface MigrationOrder {

    Short sortOrder();
    List<Class<? extends AbstractEntity<Long>>>  getOrderList();
}
