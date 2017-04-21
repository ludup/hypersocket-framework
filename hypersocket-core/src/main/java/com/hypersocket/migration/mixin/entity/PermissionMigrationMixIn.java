package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForLookUpKeys;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionMigrationMixIn extends Permission implements MigrationMixIn {

    private PermissionMigrationMixIn() {}

    @Override
    @JsonIgnore(false)
    @JsonSerialize(using = MigrationSerializerForLookUpKeys.class)
    @JsonDeserialize(using = MigrationDeserializer.class)
    public PermissionCategory getCategory() {return null;}
}
