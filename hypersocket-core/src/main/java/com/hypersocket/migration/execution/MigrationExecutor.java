package com.hypersocket.migration.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.mapper.MigrationObjectMapper;
import com.hypersocket.permissions.PermissionRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class MigrationExecutor {

    @Autowired
    MigrationObjectMapper migrationObjectMapper;

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    RealmService realmService;

    @Transactional
    public void importJson(String json) {
        try{
            ObjectMapper objectMapper = migrationObjectMapper.getObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(json);
            String className = jsonNode.get("_meta").asText();
            String entityName = jsonNode.get("name").asText();
            Class resourceClass= Class.forName(className);

            Resource resource = (Resource) realmService.findResourceInRealmByName(resourceClass, entityName);
            ObjectReader objectReader = null;
            if(resource != null) {
                objectReader = objectMapper.readerForUpdating(resource);
            } else {
                objectReader = objectMapper.readerFor(resourceClass);
            }

            resource = (Resource) objectReader.treeAsTokens(jsonNode).readValueAs(resourceClass);

            //TODO Persist resource
            System.out.println(json);
            System.out.println(resource);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            migrationCurrentStack.clearState();
        }
    }
}
