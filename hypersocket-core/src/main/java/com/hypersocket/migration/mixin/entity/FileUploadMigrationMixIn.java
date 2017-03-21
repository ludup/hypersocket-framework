package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForLookUpKeys;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.permissions.Permission;
import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Principal;
import com.hypersocket.upload.FileUpload;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadMigrationMixIn extends FileUpload implements MigrationMixIn {

    private FileUploadMigrationMixIn() {}

    @Override
    @JsonIgnore
    public String getUrl() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getContent() {
        return null;
    }
}
