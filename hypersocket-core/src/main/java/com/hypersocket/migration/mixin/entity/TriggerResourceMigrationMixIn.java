package com.hypersocket.migration.mixin.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.triggers.TriggerCondition;
import com.hypersocket.triggers.TriggerResource;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = JsonDeserializer.None.class)
public class TriggerResourceMigrationMixIn extends TriggerResource implements MigrationMixIn {

	private static final long serialVersionUID = 7166123049457882877L;

	private TriggerResourceMigrationMixIn() {}

    @Override
    @JsonDeserialize(contentUsing = MigrationDeserializer.class)
    @JsonIgnore(false)
    public Set<TriggerCondition> getConditions() {
        return null;
    }

    @Override
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
