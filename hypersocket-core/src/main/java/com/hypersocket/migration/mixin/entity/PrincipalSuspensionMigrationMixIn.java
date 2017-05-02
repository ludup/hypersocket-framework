package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalSuspension;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrincipalSuspensionMigrationMixIn extends PrincipalSuspension implements MigrationMixIn{

	private static final long serialVersionUID = 5855367347589092569L;

	private PrincipalSuspensionMigrationMixIn(){}

    @Override
    @JsonSerialize(using = MigrationSerializerForResource.class)
    @JsonDeserialize(using = MigrationDeserializer.class)
    @JsonIgnore(false)
    public Principal getPrincipal() {
        return null;
    }
}
