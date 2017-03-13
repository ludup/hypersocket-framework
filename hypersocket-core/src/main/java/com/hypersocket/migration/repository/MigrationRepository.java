package com.hypersocket.migration.repository;

import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.repository.AbstractEntity;

import java.util.List;

public interface MigrationRepository {
    <T> T findEntityByLookUpKey(Class<T> aClass, LookUpKey lookUpKey);

    void saveOrUpdate(AbstractEntity resource);

    List<AbstractEntity> findAllResourceInRealmOfType(Class aClass);
}
