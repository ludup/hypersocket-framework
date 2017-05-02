package com.hypersocket.migration.mixin.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Role;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationSchemeMigrationMixIn extends AuthenticationScheme implements MigrationMixIn {

	private static final long serialVersionUID = -357467347226649908L;

	private AuthenticationSchemeMigrationMixIn() {}

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<Role> getAllowedRoles() {
        return null;
    }

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<Role> getDeniedRoles() {
        return null;
    }
}
