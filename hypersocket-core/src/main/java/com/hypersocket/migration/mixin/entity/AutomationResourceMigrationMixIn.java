package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.automation.AutomationResource;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.triggers.TriggerResource;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationResourceMigrationMixIn extends AutomationResource implements MigrationMixIn {

	private static final long serialVersionUID = 9213384643402100633L;

	private AutomationResourceMigrationMixIn() {}

    @Override
    @JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    public Set<TriggerResource> getChildTriggers() {
        return null;
    }
}
