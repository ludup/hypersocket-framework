package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.mixin.MigrationMixIn;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationModuleMigrationMixIn extends AuthenticationModule implements MigrationMixIn {

    private AuthenticationModuleMigrationMixIn() {}

    @Override
    //@JsonSerialize(using = MigrationSerializerForResource.class)
    @JsonDeserialize(using = MigrationDeserializer.class)
    public AuthenticationScheme getScheme() {
        return null;
    }
}
