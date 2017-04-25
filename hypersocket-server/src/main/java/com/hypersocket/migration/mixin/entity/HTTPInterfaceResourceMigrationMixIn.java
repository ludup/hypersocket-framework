package com.hypersocket.migration.mixin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.migration.helper.MigrationSerializerForResource;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HTTPInterfaceResourceMigrationMixIn extends HTTPInterfaceResource implements MigrationMixIn {

	private static final long serialVersionUID = 6144823243005674611L;

	@Override
    @JsonSerialize(using = MigrationSerializerForResource.class)
    @JsonDeserialize(using = MigrationDeserializer.class)
    public CertificateResource getCertificate() {
        return null;
    }
}
