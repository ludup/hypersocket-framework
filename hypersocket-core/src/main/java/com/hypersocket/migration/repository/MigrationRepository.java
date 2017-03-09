package com.hypersocket.migration.repository;

import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.repository.AbstractEntity;

public interface MigrationRepository {
    <T> T findEntityByLookUpKey(Class<T> aClass, LookUpKey lookUpKey);

    void saveOrUpdate(AbstractEntity resource);
}
