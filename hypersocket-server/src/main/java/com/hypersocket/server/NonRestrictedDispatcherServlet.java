package com.hypersocket.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import com.hypersocket.plugins.ExtensionPlugin;
import com.hypersocket.plugins.ExtensionsPluginManager;

public class NonRestrictedDispatcherServlet extends DispatcherServlet {

	private static final long serialVersionUID = -3511464246012229321L;

	private ExtensionsPluginManager pluginManager;
	private Map<String, List<HandlerMapping>> handlerMappings = new HashMap<>();

	public NonRestrictedDispatcherServlet(WebApplicationContext webappContext, ExtensionsPluginManager pluginManager) {
		super(webappContext);
		this.pluginManager = pluginManager;
	}

	@Override
	protected void initFrameworkServlet() throws ServletException {
		super.initFrameworkServlet();
	}

	@Override
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		for (var hm : this.handlerMappings.entrySet()) {
			for (var h : hm.getValue()) {
				if (logger.isTraceEnabled()) {
					logger.trace("Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName()
							+ "'");
				}
				var handler = h.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return super.getHandler(request);
	}

	@Override
	protected void initStrategies(ApplicationContext context) {
		super.initStrategies(context);
		for (PluginWrapper pw : pluginManager.getPlugins()) {
			addHandlerMappings(pw);
		}
		pluginManager.addPluginStateListener(evt -> {
			switch (evt.getPluginState()) {
			case STARTED:
				addHandlerMappings(evt.getPlugin());
				break;
			case STOPPED:
				handlerMappings.remove(evt.getPlugin().getPluginId());
				break;
			default:
				break;
			}
		});
	}

	protected void addHandlerMappings(PluginWrapper pw) {
		try {
			addPluginHandlerMappings(pw);
		}
		catch(Throwable t) {
			logger.error("Failed to add plugin handler mappings. Stopping plugin.", t);
			try {
				pluginManager.stopPlugin(pw.getPluginId());
			}
			catch(Exception e) {
				logger.debug("Failed to stop plugin that failed to start.", e);
			}
			finally {
				pw.setPluginState(PluginState.FAILED);
				pw.setFailedException(t);
			}
		}
	}

	protected void addPluginHandlerMappings(PluginWrapper pw) {
		var pwContext = ((ExtensionPlugin) pw.getPlugin()).getWebApplicationContext();
		var matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(pwContext,
				HandlerMapping.class, true, false);
		if (!matchingBeans.isEmpty()) {
			var l = new ArrayList<HandlerMapping>(matchingBeans.values());
			handlerMappings.put(pw.getPluginId(), l);
			// We keep HandlerMappings in sorted order.
			AnnotationAwareOrderComparator.sort(l);
		}
	}

	/**
	 * Override of the default implementation to enable webdav methods
	 *
	 * @param req
	 * @param resp
	 * @throws javax.servlet.ServletException
	 * @throws java.io.IOException
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doService(req, resp);
		} catch (ServletException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}
}
