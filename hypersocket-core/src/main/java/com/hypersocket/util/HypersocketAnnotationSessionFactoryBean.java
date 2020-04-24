package com.hypersocket.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

import com.hypersocket.cache.HypersocketCacheRegionFactoryServiceInitiator;
import com.hypersocket.upgrade.UpgradeService;

public class HypersocketAnnotationSessionFactoryBean extends
		LocalSessionFactoryBean{
	
	private RegionFactory regionFactory;
	private DatabaseInformation databaseInformation;

	static Logger log = LoggerFactory.getLogger(HypersocketAnnotationSessionFactoryBean.class);
	
//	@Autowired
//	private UpgradeService upgradeService;
	
//	@Autowired
//	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Override
	public void setPackagesToScan(String...packagesToScan) {

		PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
		ArrayList<String> finalPackages = new ArrayList<>(Arrays.asList(packagesToScan));
		
		try {
			Resource[] packages = matcher.getResources("classpath*:hibernate-ext.properties");
		
			for(Resource r : packages) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(r.getInputStream()));
				
				try {
					String line;
					
					if(log.isInfoEnabled()) {
						log.info("Processing hibernate-ext.properties from " + r.getURI().toASCIIString());
					}
					while((line = reader.readLine())!=null) {
						line = line.trim();
						if(!line.startsWith("#")) {
							if(line.startsWith("scanPackage=")) {
								String pkgName = line.substring(12);
								if(log.isInfoEnabled()) {
									log.info("Will scan package " + pkgName);
								}
								finalPackages.add(pkgName + ".**");
							}
						}
					}
				} finally {
					reader.close();
				}
				
			}
		} catch (IOException e) {
		}
		
		
		super.setPackagesToScan(finalPackages.toArray(new String[0]));
	}
	
	@Override
	protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
		sfb.getStandardServiceRegistryBuilder().addInitiator(HypersocketCacheRegionFactoryServiceInitiator.INSTANCE);
		return super.buildSessionFactory(sfb);
	}
	
	@Override
	public void afterPropertiesSet() throws IOException {
//		if(environment.getActiveProfiles() != null && Arrays.asList(environment.getActiveProfiles()).contains("HA")) {
//			//needed to be done else default integrator from hibernate search will register everything before hypersocket search integrator
//			getHibernateProperties().put(HibernateSearchIntegrator.AUTO_REGISTER, false);
//		}
		String key = "hibernate.id.new_generator_mappings";
		if(databaseInformation.isClean()){
			log.info("No tables found in database from application was clean on start up, setting id gen value as true");
			getHibernateProperties().put(key, true);
		}else {
			String ormOnStartUp = databaseInformation.getOrmOnOld();
			if(StringUtils.isEmpty(ormOnStartUp)){
				log.info("Tables found in database, orm on old value is empty, setting id gen value as false");
				getHibernateProperties().put(key, true);
			}else{
				log.info(String.format("Tables found in database, orm on old value is not empty, setting id gen value as %s", ormOnStartUp));
				getHibernateProperties().put(key, Boolean.parseBoolean(ormOnStartUp));
			}
		}  

		UpgradeService upgradeService = (UpgradeService) applicationContext.getBean("upgradeService");
		getHibernateProperties().put("hibernate.cache.region.factory_class", regionFactory);
		DataSource ds = (DataSource) applicationContext.getBean("dataSource");
		upgradeService.preUpgrade(ds);

		super.afterPropertiesSet();
	}
	
	public RegionFactory getRegionFactory() {
		return regionFactory;
	}

	public void setRegionFactory(RegionFactory regionFactory) {
		this.regionFactory = regionFactory;
	}

	public DatabaseInformation getDatabaseInformation() {
		return databaseInformation;
	}

	public void setDatabaseInformation(DatabaseInformation databaseInformation) {
		this.databaseInformation = databaseInformation;
	}
}
