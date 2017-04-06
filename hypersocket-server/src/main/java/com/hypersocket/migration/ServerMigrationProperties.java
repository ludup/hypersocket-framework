package com.hypersocket.migration;

import com.hypersocket.migration.properties.MigrationProperties;
import com.hypersocket.migration.repository.MigrationCriteriaBuilder;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerMigrationProperties implements MigrationProperties {

    @Override
    public Short sortOrder() {
        return 10;
    }

    @Override
    public List<Class<? extends AbstractEntity<Long>>> getOrderList() {
        return Arrays.<Class<? extends AbstractEntity<Long>>>asList(
                HTTPInterfaceResource.class);
    }

    @Override
    public Map<Class<?>, MigrationCriteriaBuilder> getCriteriaMap() {
        return Collections.emptyMap();
    }
}
