package com.hypersocket.migration.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.hypersocket.migration.annotation.LookUpKeys;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.realm.Realm;
import com.hypersocket.repository.AbstractEntity;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MigrationUtil {

    static Logger log = LoggerFactory.getLogger(MigrationUtil.class);

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    @SuppressWarnings("unchecked")
    public LookUpKey captureEntityLookup(JsonNode node, Class aClass) {
        LookUpKeys annotation = (LookUpKeys) aClass.getAnnotation(LookUpKeys.class);
        LookUpKey lookUpKey;
        if(annotation != null) {
            lookUpKey = captureEntityLookupFromLookupKeysAnnotation(node, annotation);
        } else {
            lookUpKey = captureEntityLookup(node);
        }

        if(lookUpKey.isComposite()) {
            log.info("Processing look up key {} {}", Arrays.toString(lookUpKey.getProperties()),
                    Arrays.toString(lookUpKey.getValues()));
        } else {
            log.info("Processing look up key {} {}", lookUpKey.getProperty(), lookUpKey.getValue());
        }

        return lookUpKey;
    }

    public LookUpKey captureEntityLookup(JsonNode node) {
        LookUpKey lookUpKey = new LookUpKey();

        List<String> properties = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        JsonNode propertyNode = node.get("legacyId");
        if(propertyNode != null) {
            properties.add("legacyId");
            values.add(propertyNode.asLong());
        }

        propertyNode = node.get("name");
        if (propertyNode != null) {
            properties.add("name");
            values.add(propertyNode.asText());
        }

        propertyNode = node.get("resourceKey");
        if (propertyNode != null) {
            properties.add("resourceKey");
            values.add(propertyNode.asText());
        }

        //lookUpKey.setValue(value);

        //finally bank on id, -1 as we need some key for lookup else program would crash
        //this means here id of -1 just helps code flow, we are not looking actually for any data.
        //just to make code happy, ideally -1 value of id from database would not return any result
        //if there is value for id, we would consider it.
        if (properties.isEmpty()) {
            propertyNode = node.get("id");
            properties.add("id");
            if (propertyNode != null && NumberUtils.isNumber(propertyNode.asText())) {
                values.add(NumberUtils.createLong(propertyNode.asText()));
            } else {
                values.add(-1l);
            }
        }

        //so far not needed
        //if we need heavy customization on look up key and value property, may be we will need annotation on class
        //explicitly defining lookup key and value property
        if(properties.size() == 1) {
            lookUpKey.setProperty(properties.get(0));
            lookUpKey.setValue(values.get(0));
        } else if(properties.size() > 1) {
            lookUpKey.setComposite(true);
            lookUpKey.setProperties(properties.toArray(new String[0]));
            lookUpKey.setValues(values.toArray(new Object[0]));
        } else {
            throw new IllegalStateException(String.format("Cannot compute lookup for node %s", node));
        }

        return lookUpKey;
    }

    public LookUpKey captureEntityLookupFromLookupKeysAnnotation(JsonNode node, LookUpKeys lookUpKeys) {
        String[] propertyNames = lookUpKeys.propertyNames();
        LookUpKey lookUpKey = new LookUpKey();

        if(propertyNames.length > 1) {
            lookUpKey.setComposite(true);
            lookUpKey.setProperties(propertyNames);
            Object[] values = new Object[propertyNames.length];
            for (int i = 0;  i < propertyNames.length; ++i) {
                JsonNode jsonNode = node.get(propertyNames[i]);
                if(jsonNode.isNumber()) {
                    values[i] = jsonNode.asLong();
                } else {
                    values[i] = jsonNode.textValue();
                }
            }
            lookUpKey.setValues(values);
        } else if(propertyNames.length == 1) {
            lookUpKey.setProperty(propertyNames[0]);
            JsonNode jsonNode = node.get(propertyNames[0]);
            if(jsonNode.isNumber()) {
                lookUpKey.setValue(jsonNode.asLong());
            } else {
                lookUpKey.setValue(jsonNode.textValue());
            }

        }

        return lookUpKey;
    }

    public void fillInRealm(Object resource) {
        try {
            String property;
            if((property = getResourceRealmProperty(resource.getClass())) != null
                    && PropertyUtils.getProperty(resource, property) == null) {
                PropertyUtils.setProperty(resource, property, migrationCurrentStack.getCurrentRealm());
            }
        }catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public String getResourceRealmProperty(Class<?> resourceClass) {
        try {
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(resourceClass);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getPropertyType().isAssignableFrom(Realm.class)) {
                    return propertyDescriptor.getName();
                }
            }
            return null;
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
