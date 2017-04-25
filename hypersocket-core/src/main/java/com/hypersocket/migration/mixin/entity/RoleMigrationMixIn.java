package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForLookUpKeys;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMigrationMixIn extends Role implements MigrationMixIn {

	private static final long serialVersionUID = -2378532731449669192L;

	private RoleMigrationMixIn() {}

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForLookUpKeys.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<Permission> getPermissions() {
        return null;
    }

    @Override
    @JsonIgnore
    public Set<Principal> getPrincipals() {return null;}

}
