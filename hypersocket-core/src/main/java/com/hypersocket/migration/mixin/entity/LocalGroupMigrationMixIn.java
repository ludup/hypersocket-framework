package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalGroupMigrationMixIn extends LocalGroup implements MigrationMixIn {

	private static final long serialVersionUID = -4423025022806439898L;

	private LocalGroupMigrationMixIn() {}

    @Override
    @JsonIgnore(false)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<LocalGroup> getGroups() {
        return null;
    }

    @Override
    @JsonIgnore(false)
    @JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<LocalUser> getUsers() {
        return null;
    }
}
