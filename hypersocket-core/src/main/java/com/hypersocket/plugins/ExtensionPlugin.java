package com.hypersocket.plugins;

import java.util.LinkedHashSet;
import java.util.Set;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

public abstract class ExtensionPlugin extends SpringPlugin {
	public interface ExtensionLifecycle {
		void stop();

		void uninstall();
	}

	private ApplicationContext webApplicationContext;
	private Set<String> controllerPackages = new LinkedHashSet<String>();

	public ExtensionPlugin(PluginWrapper wrapper) {
		super(wrapper);

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
	public final void stop() {
		onStop();
		// close applicationContext
		if ((webApplicationContext != null) && (webApplicationContext instanceof ConfigurableApplicationContext)) {
			try {
				((ConfigurableApplicationContext) webApplicationContext).close();
			} finally {
				webApplicationContext = null;
			}
		}
		super.stop();
	}

	protected abstract void onStop();

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
			registerBeans(applicationContext);
			applicationContext.refresh();
		} finally {
			Thread.currentThread().setContextClassLoader(was);
		}
		return applicationContext;
	}

	public abstract void uninstall();

	protected void registerBeans(AnnotationConfigApplicationContext applicationContext) {

	}

	protected void registerWebBeans(AnnotationConfigWebApplicationContext applicationContext) {

	}

}
