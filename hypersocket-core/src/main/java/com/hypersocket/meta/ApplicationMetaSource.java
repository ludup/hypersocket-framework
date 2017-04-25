package com.hypersocket.meta;

import com.hypersocket.repository.AbstractEntity;
import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class ApplicationMetaSource {

    private MultiValueMap entityMap = new MultiValueMap();

    @PostConstruct
    private void postConstruct() {
        scanEntityClasses();
    }

    public MultiValueMap getEntityMap() {
        return entityMap;
    }

    private void scanEntityClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(AbstractEntity.class));

        final Set<BeanDefinition> classes = scanner.findCandidateComponents("com.hypersocket");

        try {
            for (BeanDefinition beanDefinition : classes) {
                Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
                entityMap.put(aClass.getSimpleName(), aClass);
            }
        }catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
