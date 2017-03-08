package com.hypersocket.migration.mixin;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class MigrationMixIn {

    @JsonIgnore
    public abstract Long getId();

    @JsonGetter("_meta")
    @JsonIgnore(false)
    public abstract String _meta();
}
