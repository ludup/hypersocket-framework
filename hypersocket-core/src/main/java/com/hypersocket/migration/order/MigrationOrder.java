package com.hypersocket.migration.order;

import com.hypersocket.repository.AbstractEntity;

import java.util.List;

public interface MigrationOrder {

    String getGroupName();
    List<Class<? extends AbstractEntity<Long>>>  getOrderList();
}
