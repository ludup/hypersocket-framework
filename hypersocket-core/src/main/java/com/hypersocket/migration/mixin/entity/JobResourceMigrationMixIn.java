package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.jobs.JobResource;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.mixin.MigrationMixIn;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobResourceMigrationMixIn extends JobResource implements MigrationMixIn {

    private JobResourceMigrationMixIn() {}

    @Override
    //@JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public JobResource getParentJob() {
        return null;
    }
}
