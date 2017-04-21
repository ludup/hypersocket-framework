package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.PermissionCategory;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PermissionCategoryMigrationMixIn extends PermissionCategory implements MigrationMixIn {

    private PermissionCategoryMigrationMixIn() {}

    @Override
    @JsonIgnore
    public Set<Permission> getPermissions() {
        return null;
    }
}
