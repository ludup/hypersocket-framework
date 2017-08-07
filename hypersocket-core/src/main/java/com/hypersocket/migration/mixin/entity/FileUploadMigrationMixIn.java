package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.upload.FileUpload;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileUploadMigrationMixIn extends FileUpload implements MigrationMixIn {

	private static final long serialVersionUID = 4197453113499184809L;

	private FileUploadMigrationMixIn() {}

    @Override
    @JsonIgnore
    public String getContent() {
        return null;
    }
}
