package com.hypersocket.migration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.Resource;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;

@Component
public class MigrationUtil {

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    public LookUpKey captureEntityLookup(JsonNode node) {
        LookUpKey lookUpKey = new LookUpKey();

        String entityName = null;

        JsonNode propertyNode = node.get("name");
        if(propertyNode != null) {
            entityName = propertyNode.asText();
            lookUpKey.setProperty("name");
        }

        if(StringUtils.isBlank(entityName)) {
            propertyNode = node.get("resourceKey");
            if(propertyNode != null) {
                entityName = propertyNode.asText();
                lookUpKey.setProperty("resourceKey");
            }
        }

        if(StringUtils.isBlank(entityName)) {
            throw new IllegalStateException(String.format("Main lookup (name/resourceKey) info not found," +
                    " cannot parse json %s", node.toString()));
        }

        lookUpKey.setValue(entityName);

        return lookUpKey;
    }

    public void fillInRealm(Object resource) {
        try {
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(resource.getClass());
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getPropertyType().isAssignableFrom(Realm.class)
                        && PropertyUtils.getProperty(resource, propertyDescriptor.getName()) == null) {
                    PropertyUtils.setProperty(resource, propertyDescriptor.getName(), migrationCurrentStack.getCurrentRealm());
                    break;
                }
            }
        }catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
