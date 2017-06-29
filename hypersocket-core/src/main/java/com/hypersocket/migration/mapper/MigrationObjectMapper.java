package com.hypersocket.migration.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.meta.ApplicationMetaSource;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.migration.mapper.module.MigrationImpExpModule;
import com.hypersocket.migration.mapper.module.StringEnhancerModule;
import com.hypersocket.migration.mixin.MigrationMixIn;
import com.hypersocket.realm.RealmService;

@Component
public class MigrationObjectMapper {

    static Logger log = LoggerFactory.getLogger(MigrationObjectMapper.class);

    @Autowired
    MigrationImpExpModule migrationImpExpModule;
    
    @Autowired
    StringEnhancerModule stringEnhancerModule;

    @Autowired
    MigrationHandlerInstantiator migrationHandlerInstantiator;

    @Autowired
    ApplicationMetaSource applicationMetaSource;
    
    @Autowired
    MigrationCurrentStack migrationCurrentStack;
    
    @Autowired
    RealmService realmService;
    
    ObjectMapper objectMapper;

    Map<String, Class<?>> customMixInMap = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        objectMapper = new ObjectMapper();
        
        objectMapper.registerModule(migrationImpExpModule);
        objectMapper.registerModule(stringEnhancerModule);
        
        objectMapper.setHandlerInstantiator(migrationHandlerInstantiator);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        scanMigrationMixIn();
        addMigrationMixIn();

    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private void addMigrationMixIn() {
        MultiValueMap entityMap = applicationMetaSource.getEntityMap();
        for (Object object : entityMap.values()) {
            Class<?> aClass = (Class<?>) object;
            objectMapper.addMixIn(aClass, MigrationMixIn.class);
            String migrationMixInKey = String.format("%sMigrationMixIn", aClass.getSimpleName());
            if(customMixInMap.containsKey(migrationMixInKey)) {
                objectMapper.addMixIn(aClass, customMixInMap.get(migrationMixInKey));
            }
        }
    }

    private void scanMigrationMixIn() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationMixIn.class));

        final Set<BeanDefinition> classes = scanner.findCandidateComponents("com.hypersocket.migration.mixin.entity");

        try {
            for (BeanDefinition beanDefinition : classes) {
                String className = beanDefinition.getBeanClassName();
                Class<?> aClass = MigrationObjectMapper.class.getClassLoader().loadClass(className);
                Class<?> oldClass = customMixInMap.put(aClass.getSimpleName(), aClass);
                if(oldClass != null) {
                    throw new IllegalStateException(String.format("While adding class for key %s, map returned back object, " +
                            "meaning value already exists, " +
                            "for single key we have multiple values, " +
                            "please rename classes for error key.", aClass.getSimpleName()));
                }
            }
        }catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
