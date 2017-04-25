package com.hypersocket.migration.mixin.entity;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.attributes.user.UserAttribute;
import com.hypersocket.attributes.user.UserAttributeCategory;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Role;

public class UserAttributeMigrationMixIn extends UserAttribute implements MigrationMixIn{

	private static final long serialVersionUID = 6441279417398061366L;

	private UserAttributeMigrationMixIn(){}

    @Override
    @JsonSerialize(using = MigrationSerializerForResource.class)
    @JsonDeserialize(using = MigrationDeserializer.class)
    public UserAttributeCategory getCategory() {
        return null;
    }

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<Role> getRoles() {return null;}
}
