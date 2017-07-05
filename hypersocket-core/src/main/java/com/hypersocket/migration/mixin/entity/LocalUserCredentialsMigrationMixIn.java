package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserCredentials;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;

public class LocalUserCredentialsMigrationMixIn extends LocalUserCredentials implements MigrationMixIn {

	private static final long serialVersionUID = 3202313583905597021L;

	private LocalUserCredentialsMigrationMixIn() {}
	
	@Override
	@JsonSerialize(using = MigrationSerializerForResource.class)
    @JsonDeserialize(using = MigrationDeserializer.class)
	public LocalUser getUser() { return null; }
	
}
