package com.hypersocket.migration.mixin;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface MigrationMixIn {

    @JsonIgnore
    Long getId();

    @JsonGetter("_meta")
    @JsonIgnore(false)
    String _meta();

    @JsonIgnore
    Date getModifiedDate();

    @JsonIgnore
    Date getCreateDate();
}
