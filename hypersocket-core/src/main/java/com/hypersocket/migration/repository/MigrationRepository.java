package com.hypersocket.migration.repository;

import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.AbstractResource;
import org.hibernate.criterion.DetachedCriteria;

import java.util.List;

public interface MigrationRepository {
    <T> T findEntityByLookUpKey(Class<T> aClass, LookUpKey lookUpKey, Realm realm);

    void saveOrUpdate(AbstractEntity resource);

    <T> List<T> findAllResourceInRealmOfType(Class<T> aClass, Realm realm);

    List<DatabaseProperty> findAllDatabaseProperties(AbstractResource abstractResource);

    <T> T findEntityByLegacyIdInRealm(Class<? extends AbstractResource> aClass, Long legacyId, Realm realm);

    <T> T findEntityByNameLookUpKey(Class<T> aClass, LookUpKey lookUpKey, Realm realm);

    <T> DetachedCriteria buildCriteriaFor(Class<T> aClass, String alias);

    List executeCriteria(DetachedCriteria criteria);

    void flush();
}
