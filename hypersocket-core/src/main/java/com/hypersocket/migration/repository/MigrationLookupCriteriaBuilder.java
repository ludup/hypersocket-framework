package com.hypersocket.migration.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.realm.Realm;
import org.hibernate.criterion.DetachedCriteria;

public interface MigrationLookupCriteriaBuilder {
    DetachedCriteria make(Realm realm, LookUpKey lookUpKey, JsonNode node);
}
