package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.realm.Realm;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RealmMigrationMixIn extends Realm implements MigrationMixIn {

	private static final long serialVersionUID = -8170514503121995244L;

	private RealmMigrationMixIn() {}
}
