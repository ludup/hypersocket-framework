package com.hypersocket.migration.order;

import com.hypersocket.repository.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class MigrationOrderInfoProvider {

    static Logger log = LoggerFactory.getLogger(MigrationOrderInfoProvider.class);

    private Map<Short, List<Class<? extends AbstractEntity<Long>>>> migrationOrderMap = new TreeMap<>();

    @PostConstruct
    private void postConstruct() {
        scanForMigrationOrderClasses();
    }

    public Map<Short, List<Class<? extends AbstractEntity<Long>>>> getMigrationOrderMap() {
        return Collections.unmodifiableMap(migrationOrderMap);
    }

    private void scanForMigrationOrderClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationOrder.class));

        final Set<BeanDefinition> classes = scanner.findCandidateComponents("com.hypersocket.migration");

        try {
            for (BeanDefinition beanDefinition : classes) {
                String className = beanDefinition.getBeanClassName();
                Class aClass = MigrationOrderInfoProvider.class.getClassLoader().loadClass(className);
                MigrationOrder instance = (MigrationOrder) aClass.newInstance();
                migrationOrderMap.put(instance.sortOrder(), instance.getOrderList());
            }
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
