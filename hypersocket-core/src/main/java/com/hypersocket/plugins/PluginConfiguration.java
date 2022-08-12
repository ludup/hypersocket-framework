package com.hypersocket.plugins;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.boot.MetadataSources;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.orm.hibernate5.HibernateTransactionManager;

import com.hypersocket.util.HypersocketAnnotationSessionFactoryBean;

public abstract class PluginConfiguration {
	
	@Autowired
	private ExtensionsPluginManager pluginManager;

	protected FactoryBean<DataSource> dataSource() {
		/* Datasource will usually be the containers datasource */
		return new FactoryBean<DataSource>() {

			@Override
			public DataSource getObject() throws Exception {
				return pluginManager.getDataSource();
			}

			@Override
			public Class<?> getObjectType() {
				return DataSource.class;
			}

			@Override
			public boolean isSingleton() {
				return true;
			}
		};
	}
	
	protected HypersocketAnnotationSessionFactoryBean sessionFactory() throws Exception {

		var originalSessionFactory = pluginManager.getSessionFactory();
	    var hibernateProperties = originalSessionFactory.getHibernateProperties();
		var newHibernateProperties = new Properties();
		var orginalMeta = originalSessionFactory.getMetadataSources();
		newHibernateProperties.putAll(hibernateProperties);
		
		var newMeta = new MetadataSources(/*orginalMeta.getServiceRegistry()*/);
		for(var c : orginalMeta.getAnnotatedClasses())
			newMeta.addAnnotatedClass(c);
		for(var c : orginalMeta.getAnnotatedClassNames())
			newMeta.addAnnotatedClassName(c);
		for(var c : orginalMeta.getAnnotatedPackages())
			newMeta.addPackage(c);
//		newMeta.addAnnotatedClass(InventoryAgentResource.class);
	    
	    var sessionFactory = new HypersocketAnnotationSessionFactoryBean();
	    //sessionFactory.setPackagesToScan("com.hypersocket.**, com.logonbox.**, " + InventoryConfiguration.class.getPackage().getName() + ".**");
	    sessionFactory.setPackagesToScan(getClass().getPackage().getName() + ".**");
	    sessionFactory.setMetadataSources(newMeta);
		sessionFactory.setDataSource(dataSource().getObject());
		sessionFactory.setResourceLoader(new DefaultResourceLoader(getClass().getClassLoader()));
		sessionFactory.setDatabaseInformation(originalSessionFactory.getDatabaseInformation());
		sessionFactory.setRegionFactory(originalSessionFactory.getRegionFactory());

	    sessionFactory.setHibernateProperties(newHibernateProperties);

	    return sessionFactory;
	}

	protected HibernateTransactionManager transactionManager() throws Exception {
	    HibernateTransactionManager transactionManager = new HibernateTransactionManager();
	    transactionManager.setSessionFactory(sessionFactory().getObject());
	    return transactionManager;
	}
}
