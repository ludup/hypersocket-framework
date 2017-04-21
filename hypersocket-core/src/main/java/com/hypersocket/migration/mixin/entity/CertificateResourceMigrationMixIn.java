package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.migration.mixin.MigrationMixIn;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificateResourceMigrationMixIn extends CertificateResource implements MigrationMixIn{

    private CertificateResourceMigrationMixIn() {}

    @Override
    @JsonIgnore(false)
    public String getPrivateKey() {
        return null;
    }
}
