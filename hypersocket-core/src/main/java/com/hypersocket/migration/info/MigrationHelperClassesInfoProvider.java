package com.hypersocket.migration.info;

import com.hypersocket.migration.exporter.MigrationExporter;
import com.hypersocket.migration.importer.MigrationImporter;
import com.hypersocket.migration.properties.MigrationProperties;
import com.hypersocket.migration.repository.MigrationExportCriteriaBuilder;
import com.hypersocket.migration.repository.MigrationLookupCriteriaBuilder;
import com.hypersocket.repository.AbstractEntity;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class MigrationHelperClassesInfoProvider {

    static Logger log = LoggerFactory.getLogger(MigrationHelperClassesInfoProvider.class);

    @Autowired
    ApplicationContext applicationContext;

    private Map<Short, List<Class<? extends AbstractEntity<Long>>>> migrationOrderMap = MultiValueMap.decorate(new TreeMap<>());
    private Map<Class<?>, MigrationImporter> migrationImporterMap = new HashMap<>();
    private Map<Class<?>, MigrationExporter> migrationExporterMap = new HashMap<>();
    private Map<Class<?>, MigrationExportCriteriaBuilder> migrationExportCriteriaBuilder = new HashMap<>();
    private Map<Class<?>, MigrationLookupCriteriaBuilder> migrationLookupCriteriaBuilder = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        scanForMigrationHelperClasses();
    }

    public Map<Short, List<Class<? extends AbstractEntity<Long>>>> getMigrationOrderMap() {
        return migrationOrderMap;
    }

    public Map<Class<?>, MigrationImporter> getMigrationImporterMap() {
        return Collections.unmodifiableMap(migrationImporterMap);
    }

    public Map<Class<?>, MigrationExporter> getMigrationExporterMap() {
        return Collections.unmodifiableMap(migrationExporterMap);
    }

    public Map<Class<?>, MigrationExportCriteriaBuilder> getMigrationExportCriteriaBuilder() {
        return Collections.unmodifiableMap(migrationExportCriteriaBuilder);
    }

    public Map<Class<?>, MigrationLookupCriteriaBuilder> getMigrationLookupCriteriaBuilder() {
        return Collections.unmodifiableMap(migrationLookupCriteriaBuilder);
    }

    private void scanForMigrationHelperClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationProperties.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationImporter.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationExporter.class));

        final Set<BeanDefinition> classes = scanner.findCandidateComponents("com.hypersocket.migration");

        try {
            for (BeanDefinition beanDefinition : classes) {
                String className = beanDefinition.getBeanClassName();
                Class aClass = MigrationHelperClassesInfoProvider.class.getClassLoader().loadClass(className);
                if(MigrationProperties.class.isAssignableFrom(aClass)) {
                    MigrationProperties instance = (MigrationProperties) aClass.newInstance();
                    migrationOrderMap.put(instance.sortOrder(), instance.getOrderList());
                    Map<Class<?>, MigrationExportCriteriaBuilder> criteriaMap = instance.getExportCriteriaMap();
                    if(criteriaMap != null) {
                        migrationExportCriteriaBuilder.putAll(instance.getExportCriteriaMap());
                    }
                    Map<Class<?>, MigrationLookupCriteriaBuilder> lookupCriteriaMap = instance.getLookupCriteriaMap();
                    if(lookupCriteriaMap != null) {
                        migrationLookupCriteriaBuilder.putAll(instance.getLookupCriteriaMap());
                    }
                } else if (MigrationImporter.class.isAssignableFrom(aClass)) {
                    MigrationImporter instance = (MigrationImporter) applicationContext.getBean(aClass.getCanonicalName());
                    migrationImporterMap.put(instance.getType(), instance);
                } else if (MigrationExporter.class.isAssignableFrom(aClass)) {
                    MigrationExporter instance = (MigrationExporter) applicationContext.getBean(aClass.getCanonicalName());
                    migrationExporterMap.put(instance.getType(), instance);
                }
            }
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<String> getAllExportableClasses() {
        List<String> exportable = new ArrayList<>();
        Set<Short> keys = migrationOrderMap.keySet();
        for (Short key : keys) {
            Collection migrationClassesList = ((MultiValueMap) migrationOrderMap).getCollection(key);
            for (Object migrationClasses : migrationClassesList) {
                for (Class<? extends AbstractEntity<Long>> aClass : (List<Class<? extends AbstractEntity<Long>>>) migrationClasses) {
                    exportable.add(aClass.getSimpleName());
                }
            }
        }
        return exportable;
    }
}
