package com.hypersocket.migration.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.hypersocket.migration.customized.MigrationCustomExport;
import com.hypersocket.migration.customized.MigrationCustomImport;
import com.hypersocket.migration.exporter.MigrationExporter;
import com.hypersocket.migration.importer.MigrationImporter;
import com.hypersocket.migration.properties.MigrationProperties;
import com.hypersocket.migration.repository.MigrationExportCriteriaBuilder;
import com.hypersocket.migration.repository.MigrationLookupCriteriaBuilder;
import com.hypersocket.repository.AbstractEntity;

@Component
public class MigrationHelperClassesInfoProvider {

    static Logger log = LoggerFactory.getLogger(MigrationHelperClassesInfoProvider.class);

    @Autowired
    ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
	private Map<Short, List<Class<? extends AbstractEntity<Long>>>> migrationOrderMap = MultiValueMap.decorate(new TreeMap<>());
    private Map<Class<?>, MigrationImporter<AbstractEntity<Long>>> migrationImporterMap = new HashMap<>();
    private Map<Class<?>, MigrationExporter<AbstractEntity<Long>>> migrationExporterMap = new HashMap<>();
    private Map<Class<?>, MigrationExportCriteriaBuilder> migrationExportCriteriaBuilder = new HashMap<>();
    private Map<Class<?>, MigrationLookupCriteriaBuilder> migrationLookupCriteriaBuilder = new HashMap<>();
    
    private Map<String, MigrationCustomImport<?>> migrationCustomImports = new HashMap<>();
    
    private Set<MigrationCustomExport<?>> migrationCustomExports = new TreeSet<>(new Comparator<MigrationCustomExport<?>>() {

		@Override
		public int compare(MigrationCustomExport<?> o1, MigrationCustomExport<?> o2) {
			return o1.sortOrder().compareTo(o2.sortOrder());
		}
	});

    @PostConstruct
    private void postConstruct() {
        scanForMigrationHelperClasses();
    }

    public Map<Short, List<Class<? extends AbstractEntity<Long>>>> getMigrationOrderMap() {
        return migrationOrderMap;
    }

    public Map<Class<?>, MigrationImporter<AbstractEntity<Long>>> getMigrationImporterMap() {
        return Collections.unmodifiableMap(migrationImporterMap);
    }

    public Map<Class<?>, MigrationExporter<AbstractEntity<Long>>> getMigrationExporterMap() {
        return Collections.unmodifiableMap(migrationExporterMap);
    }

    public Map<Class<?>, MigrationExportCriteriaBuilder> getMigrationExportCriteriaBuilder() {
        return Collections.unmodifiableMap(migrationExportCriteriaBuilder);
    }

    public Map<Class<?>, MigrationLookupCriteriaBuilder> getMigrationLookupCriteriaBuilder() {
        return Collections.unmodifiableMap(migrationLookupCriteriaBuilder);
    }
    
    public Map<String, MigrationCustomImport<?>> getMigrationCustomImports() {
    	return Collections.unmodifiableMap(migrationCustomImports);
    }
    
    public Collection<MigrationCustomExport<?>> getMigrationCustomExports() {
    	return Collections.unmodifiableCollection(migrationCustomExports);
    }
    
    @SuppressWarnings("unchecked")
	private void scanForMigrationHelperClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationProperties.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationImporter.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationExporter.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationCustomImport.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(MigrationCustomExport.class));

        final Set<BeanDefinition> classes = scanner.findCandidateComponents("com.hypersocket.migration");

        try {
            for (BeanDefinition beanDefinition : classes) {
                String className = beanDefinition.getBeanClassName();
                Class<?> aClass = MigrationHelperClassesInfoProvider.class.getClassLoader().loadClass(className);
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
                    MigrationImporter<AbstractEntity<Long>> instance = (MigrationImporter<AbstractEntity<Long>>) applicationContext.getBean(aClass.getCanonicalName());
                    migrationImporterMap.put(instance.getType(), instance);
                } else if (MigrationExporter.class.isAssignableFrom(aClass)) {
                    MigrationExporter<AbstractEntity<Long>> instance = (MigrationExporter<AbstractEntity<Long>>) applicationContext.getBean(aClass.getCanonicalName());
                    migrationExporterMap.put(instance.getType(), instance);
                } else if (MigrationCustomImport.class.isAssignableFrom(aClass)) {
                	MigrationCustomImport<?> instance = (MigrationCustomImport<?>) applicationContext.getBean(aClass.getCanonicalName());
                	migrationCustomImports.put(instance.getType().getSimpleName(), instance);
                } else if (MigrationCustomExport.class.isAssignableFrom(aClass)) {
                	MigrationCustomExport<?> instance = (MigrationCustomExport<?>) applicationContext.getBean(aClass.getCanonicalName());
                	migrationCustomExports.add(instance);
                }
            }
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({ "unchecked" })
	public List<String> getAllExportableClasses() {
        List<String> exportable = new ArrayList<>();
        Set<Short> keys = migrationOrderMap.keySet();
        for (Short key : keys) {
            Collection<?> migrationClassesList = ((MultiValueMap) migrationOrderMap).getCollection(key);
            for (Object migrationClasses : migrationClassesList) {
                for (Class<? extends AbstractEntity<Long>> aClass : (List<Class<? extends AbstractEntity<Long>>>) migrationClasses) {
                    exportable.add(aClass.getSimpleName());
                }
            }
        }
        return exportable;
    }
}
