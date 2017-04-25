package com.hypersocket.migration.repository;

import com.hypersocket.realm.Realm;
import org.hibernate.criterion.DetachedCriteria;

public interface MigrationExportCriteriaBuilder {
    DetachedCriteria make(Realm realm);
}
