package com.hypersocket.migration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

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

        lookUpKey.setValue(entityName);

        //finally bank on id, -1 as we need some key for lookup else program would crash
        //this means here id of -1 just helps code flow, we are not looking actually for any data.
        //just to make code happy, ideally -1 value of id from database would not return any result
        //if there is value for id, we would consider it.
        if(StringUtils.isBlank(entityName)) {
            propertyNode = node.get("id");
            lookUpKey.setProperty("id");
            if(propertyNode != null && NumberUtils.isNumber(propertyNode.asText())) {
                lookUpKey.setValue(NumberUtils.createLong(propertyNode.asText()));
            } else {
                lookUpKey.setValue(-1l);
            }
        }

        //so far not needed
        //if we need heavy customization on look up key and value property, may be we will need annotation on class
        //explicitly defining lookup key and value property

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

    public String getMappedBy(AbstractEntity object, final String property) {
        final StringBuilder mappedBy = new StringBuilder();
        ReflectionUtils.doWithFields(object.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Annotation[] annotations = field.getAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof OneToMany) {
                            mappedBy.append(((OneToMany) annotation).mappedBy());
                        } else if (annotation instanceof OneToOne) {
                            mappedBy.append(((OneToOne) annotation).mappedBy());
                        }
                    }
                }
            }
        }, new ReflectionUtils.FieldFilter() {
            @Override
            public boolean matches(Field field) {
                return field.getName().equals(property);
            }
        });
        return mappedBy.toString();

    }
}
