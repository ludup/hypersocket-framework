package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.local.LocalGroup;
import com.hypersocket.local.LocalUser;
import com.hypersocket.migration.mixin.MigrationMixIn;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalUserMigrationMixIn extends LocalUser implements MigrationMixIn {

	private static final long serialVersionUID = 2032037336768069863L;

	private LocalUserMigrationMixIn() {}

    @Override
    @JsonIgnore
    public Set<LocalGroup> getGroups() {
        return null;
    }
}
