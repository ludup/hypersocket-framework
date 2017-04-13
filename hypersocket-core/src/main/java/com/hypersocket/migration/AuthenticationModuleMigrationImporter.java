package com.hypersocket.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.auth.AuthenticationModule;
import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.migration.importer.MigrationImporter;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("com.hypersocket.migration.AuthenticationModuleMigrationImporter")
public class AuthenticationModuleMigrationImporter implements MigrationImporter<AuthenticationModule>{

    static Logger log = LoggerFactory.getLogger(AuthenticationModuleMigrationImporter.class);

    @Autowired
    MigrationRepository migrationRepository;

    @Override
    public void process(AuthenticationModule authenticationModule) {
        AuthenticationScheme authenticationScheme = authenticationModule.getScheme();

        //is transient
        if(authenticationScheme != null && authenticationScheme.getId() == null) {
            log.info("New auth scheme found with name {}", authenticationScheme.getName());
            log.info("Auth scheme is attached to auth module with template {}", authenticationModule.getTemplate());

            migrationRepository.saveOrUpdate(authenticationScheme);
        }
    }

    @Override
    public void postSave(AuthenticationModule authenticationModule) {

    }

    @Override
    public Class<AuthenticationModule> getType() {
        return AuthenticationModule.class;
    }

    @Override
    public void processCustomOperationsMap(JsonNode jsonNode, Realm realm) {

    }
}
