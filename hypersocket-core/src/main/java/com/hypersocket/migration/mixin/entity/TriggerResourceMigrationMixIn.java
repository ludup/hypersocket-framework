package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.mapper.MigrationBeanDeserializer;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = JsonDeserializer.None.class)
public class TriggerResourceMigrationMixIn extends TriggerResource implements MigrationMixIn {

    private TriggerResourceMigrationMixIn() {}

    @Override
    //@JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    @JsonIgnore(false)
    public Set<TriggerCondition> getConditions() {
        return null;
    }

    @Override
    //@JsonSerialize(contentUsing = MigrationSerializerForResource.class)
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    @JsonIgnore(false)
    public Set<TriggerResource> getChildTriggers() {
        return null;
    }

    @Override
    @JsonIgnore
    public Set<TriggerCondition> getAllConditions() {
        return null;
    }

    @Override
    @JsonIgnore
    public Set<TriggerCondition> getAnyConditions() {
        return null;
    }
}
