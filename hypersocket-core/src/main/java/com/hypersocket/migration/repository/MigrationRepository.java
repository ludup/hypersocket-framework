package com.hypersocket.migration.repository;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.AbstractResource;

public interface MigrationRepository {
    <T> T findEntityByLookUpKey(Class<T> aClass, LookUpKey lookUpKey, Realm realm);

    void saveOrUpdate(AbstractEntity<?> resource);

    <T> List<T> findAllResourceInRealmOfType(Class<T> aClass, Realm realm);

    List<DatabaseProperty> findAllDatabaseProperties(AbstractResource abstractResource);

    <T> T findEntityByLegacyIdInRealm(Class<? extends AbstractEntity> aClass, Long legacyId, Realm realm);

    <T> T findEntityByNameLookUpKey(Class<T> aClass, LookUpKey lookUpKey, Realm realm);

    <T> DetachedCriteria buildCriteriaFor(Class<T> aClass, String alias);

    List<?> executeCriteria(DetachedCriteria criteria);

    Realm findRealm(LookUpKey lookUpKey);

    void flush();
}
