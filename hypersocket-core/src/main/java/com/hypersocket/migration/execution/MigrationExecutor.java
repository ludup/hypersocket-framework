package com.hypersocket.migration.execution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.migration.util.MigrationUtil;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MigrationExecutor {

    @Autowired
    MigrationObjectMapper migrationObjectMapper;

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    @Autowired
    MigrationUtil migrationUtil;

    @Autowired
    MigrationRepository migrationRepository;

    @Autowired
    RealmService realmService;

    @Transactional
    public void importJson(String json, Realm realm) {
        try{
            if(realm == null) {
                realm = realmService.getCurrentRealm();
            }

            migrationCurrentStack.addRealm(realm);

            ObjectMapper objectMapper = migrationObjectMapper.getObjectMapper();
            JsonNode node = objectMapper.readTree(json);
            String className = node.get("_meta").asText();

            if(StringUtils.isBlank(className)) {
                throw new IllegalStateException(String.format("Class type info not found, cannot parse json %s",
                        node.toString()));
            }

            LookUpKey lookUpKey = migrationUtil.captureEntityLookup(node);

            Class resourceClass= MigrationExecutor.class.getClassLoader().loadClass(className);

            AbstractEntity resource = (AbstractEntity) migrationRepository.findEntityByLookUpKey(resourceClass, lookUpKey);
            if(resource == null) {
                resource = (AbstractEntity) resourceClass.newInstance();
            }
            ObjectReader objectReader = objectMapper.readerForUpdating(resource);

            resource = (Resource) objectReader.treeAsTokens(node).readValueAs(resourceClass);

            migrationUtil.fillInRealm(resource);

            migrationRepository.saveOrUpdate(resource);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            migrationCurrentStack.clearState();
        }
    }
}
