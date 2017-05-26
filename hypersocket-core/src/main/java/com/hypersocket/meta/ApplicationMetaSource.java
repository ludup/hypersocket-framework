package com.hypersocket.meta;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.hypersocket.repository.AbstractEntity;

@Component
public class ApplicationMetaSource {

    private MultiValueMap entityMap = new MultiValueMap();

    @PostConstruct
    private void postConstruct() {
        scanEntityClassesInHypersocketPackage();
        scanEntityClassesInNervepointPackage();
    }

    public MultiValueMap getEntityMap() {
        return entityMap;
    }

    private void scanEntityClassesInHypersocketPackage() {
        scanEntityClasses("com.hypersocket");
    }
    
    private void scanEntityClassesInNervepointPackage() {
        scanEntityClasses("com.nervepoint");
    }

	private void scanEntityClasses(String _package) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(AbstractEntity.class));

        final Set<BeanDefinition> classes = scanner.findCandidateComponents(_package);

        try {
            for (BeanDefinition beanDefinition : classes) {
                Class aClass = Class.forName(beanDefinition.getBeanClassName());
                entityMap.put(aClass.getSimpleName(), aClass);
            }
        }catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
	}
}
