package com.hypersocket.plugins;

import java.util.LinkedHashSet;
import java.util.Set;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

public abstract class ExtensionPlugin extends SpringPlugin {
	private ApplicationContext webApplicationContext;
	private final Set<String> controllerPackages = new LinkedHashSet<String>();
	private final Class<?> configurationClass;

	public ExtensionPlugin(PluginWrapper wrapper, Class<?> configurationClass) {
		super(wrapper);
		this.configurationClass = configurationClass;

		/*
		 * The context is the whole 'webapp'. It's path is the base path, i.e.
		 * /product-name
		 */
		controllerPackages.add("com.hypersocket.json.**");
		controllerPackages.add("com.hypersocket.**.json");

		controllerPackages.add("com.logonbox.json.**");
		controllerPackages.add("com.logonbox.**.json");
	}

	public final ApplicationContext getWebApplicationContext() {
		if (webApplicationContext == null) {
			webApplicationContext = createWebApplicationContext();
		}

		return webApplicationContext;
	}


    @Override
    public final void start() {
    	beforeStart();
    	super.start();
    	afterStart();
    }

	public final void uninstall(boolean deleteData) throws Exception {
		beforeUninstall(deleteData);
		Exception exception = null;
		for(var bean : getApplicationContext().getBeansOfType(ExtensionLifecycle.class).values()) {
			try {
				bean.uninstall(deleteData);
			} catch (Exception e) {
				if(exception == null)
					exception = e;
			}
		}
		afterUninstall(deleteData);
		if(exception != null)
			throw exception;
	}

	@Override
	public final void stop() {
		beforeStop();
		for(var bean : getApplicationContext().getBeansOfType(ExtensionLifecycle.class).values()) {
			bean.stop();
		}
		
		// close applicationContext
		if ((webApplicationContext != null) && (webApplicationContext instanceof ConfigurableApplicationContext)) {
			try {
				((ConfigurableApplicationContext) webApplicationContext).close();
			} finally {
				webApplicationContext = null;
			}
		}
		super.stop();
		afterStop();
	}

	protected void beforeStop() {
	}

	protected void beforeStart() {
	}

	protected void afterStop() {
	}

	protected void afterStart() {
	}

	protected ApplicationContext createWebApplicationContext() {

		var webApplicationContext = new AnnotationConfigWebApplicationContext();
		var pm = (ExtensionsPluginManager) wrapper.getPluginManager();
		webApplicationContext.setParent(getApplicationContext());

		var servletContext = new ExtensionServletContext(pm.getServletContext());

		var cl = getWrapper().getPluginClassLoader();
		var was = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		try {
			webApplicationContext.setClassLoader(cl);
			webApplicationContext.setServletContext(servletContext);
			webApplicationContext.setServletConfig(pm.getServletConfig());
//			webApplicationContext.scan(controllerPackages.toArray(new String[0]));
			webApplicationContext.register(configurationClass);
			webApplicationContext.register(DelegatingWebMvcConfiguration.class);
			registerWebBeans(webApplicationContext);
			webApplicationContext.refresh();
			webApplicationContext.start();
		} finally {
			Thread.currentThread().setContextClassLoader(was);
		}
		return webApplicationContext;
	}

	@Override
	protected final ApplicationContext createApplicationContext() {
		var pm = (ExtensionsPluginManager) wrapper.getPluginManager();
		var context = pm.getApplicationContext();
		
		var applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.setParent(context);
		var cl = getWrapper().getPluginClassLoader();
		var was = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		try {
			applicationContext.setClassLoader(cl);
			applicationContext.register(configurationClass);
			registerBeans(applicationContext);
			
			
//			 // create the datasource bean
//		    BeanDefinitionBuilder dataSourceBeanBuilder = BeanDefinitionBuilder.rootBeanDefinition(DataSourceConfiguration.class, "createDataSource");
//		    dataSourceBeanBuilder.addConstructorArgValue(descriptor.getDataSourceDescriptor().getJNDILookupName());
//		    dataSourceBeanBuilder.addConstructorArgValue(descriptor.getDataSourceDescriptor().isResourceRef());
//		    applicationContext.registerBeanDefinition("dataSource", dataSourceBeanBuilder.getBeanDefinition());
//
//		    // now build the sessionFactor
//		    BeanDefinitionBuilder sessionFactoryBeanBuilder = BeanDefinitionBuilder.rootBeanDefinition(SessionFactoryFactory.class, "createSessionFactory");
//		    sessionFactoryBeanBuilder.addConstructorArgReference("dataSource");
//		    sessionFactoryBeanBuilder.addConstructorArgValue(module.getKey());
//		    sessionFactoryBeanBuilder.addConstructorArgValue(moduleContext.getModuleResourceLoader());
//		    sessionFactoryBeanBuilder.addConstructorArgValue(annotatedClasses);
//		    applicationContext.registerBeanDefinition("sessionFactory", sessionFactoryBeanBuilder.getBeanDefinition());
//
//		    // now build the transactionManager
//		    BeanDefinitionBuilder transactionManagerBeanBuilder = BeanDefinitionBuilder.rootBeanDefinition(HibernateConfigurationFactory.class, "createTransactionManager");
//		    transactionManagerBeanBuilder.addConstructorArgReference("sessionFactory");
//		    applicationContext.registerBeanDefinition("transactionManager", transactionManagerBeanBuilder.getBeanDefinition());
//			
//			
			
			applicationContext.refresh();
			applicationContext.start();
			
			applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
		} finally {
			Thread.currentThread().setContextClassLoader(was);
		}
		return applicationContext;
	}

	protected void beforeUninstall(boolean deleteData) {
	}

	protected void afterUninstall(boolean deleteData) {
	}

	protected void registerBeans(AnnotationConfigApplicationContext applicationContext) {

	}

	protected void registerWebBeans(AnnotationConfigWebApplicationContext applicationContext) {

	}

}
