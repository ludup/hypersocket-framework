package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForPermission;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;

import java.util.Date;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMigrationMixIn extends Role implements MigrationMixIn {

    @Override
    @JsonIgnore
    public Long getId() {return null;}

    @Override
    @JsonGetter("_meta")
    @JsonIgnore(false)
    public String _meta() {return null;}

    @Override
    @JsonIgnore
    public Date getModifiedDate() {
        return null;
    }

    @Override
    @JsonIgnore
    public Date getCreateDate() {
        return null;
    }

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForPermission.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<Permission> getPermissions() {
        return null;
    }

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<Principal> getPrincipals() {return null;}

}
