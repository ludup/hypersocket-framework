package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializer;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.realm.Principal;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMigrationMixIn extends MigrationMixIn {

    private RoleMigrationMixIn() {}

    @Override
    @JsonIgnore
    public Long getId() {return null;}

    @Override
    @JsonGetter("_meta")
    @JsonIgnore(false)
    public String _meta() {return null;}

    @JsonSerialize(contentUsing = MigrationSerializer.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    Set<Principal> getPrincipals() {return null;};

}
