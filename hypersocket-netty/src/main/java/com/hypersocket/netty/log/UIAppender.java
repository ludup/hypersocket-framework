package com.hypersocket.netty.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.slf4j.event.EventConstants;
import org.slf4j.event.Level;

import com.hypersocket.server.LoggingOutputListener;
import com.hypersocket.server.LoggingOutputListener.LoggingOutputEvent;

@Plugin(name = "UIAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class UIAppender extends AbstractAppender {

	private ExecutorService queue = Executors.newSingleThreadExecutor();
	private static UIAppender instance;
	private List<LoggingOutputListener> listeners = new ArrayList<>();

	@PluginFactory
	public static UIAppender createAppender(@PluginAttribute("name") String name,
			@PluginElement("Filter") Filter filter, @PluginElement("Layout") Layout<? extends Serializable> layout) {
		return new UIAppender(name, filter, layout, false, new Property[0]);
	}

	public UIAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions,
			Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}

	{
		instance = this;
	}

	public void addListener(LoggingOutputListener listener) {
		listeners.add(listener);
	}

	public void removeListener(LoggingOutputListener listener) {
		listeners.remove(listener);
	}

	public static UIAppender getInstance() {
		return instance;
	}

	@Override
	public void append(LogEvent event) {
		queue.execute(() -> {
			var uiLogEvent = toUILogEvent(event);
			for (int i = listeners.size() - 1; i >= 0; i--) {
				listeners.get(i).logEvent(uiLogEvent);
			}
		});
	}

	private LoggingOutputEvent toUILogEvent(LogEvent event) {
		Level level;
		int syslogLevel;
		switch (event.getLevel().intLevel()) {
		case EventConstants.TRACE_INT:
			level = Level.TRACE;
			syslogLevel = 7;
			break;
		case EventConstants.DEBUG_INT:
			level = Level.DEBUG;
			syslogLevel = 7;
			break;
		case EventConstants.ERROR_INT:
			level = Level.ERROR;
			syslogLevel = 3;
			break;
		case EventConstants.WARN_INT:
			level = Level.WARN;
			syslogLevel = 4;
			break;
		default:
			level = Level.INFO;
			syslogLevel = 6;
			break;
		}
		return new LoggingOutputEvent(event.getTimeMillis(), event.getMessage().getFormattedMessage(), level,
				syslogLevel, event.getLoggerName(), event.getSource() == null ? null : event.getSource().getClassName(),
				event.getSource() == null ? null : event.getSource().getFileName(),
				event.getSource() == null ? null : event.getSource().getMethodName(),
				event.getSource() == null ? null : String.valueOf(event.getSource().getLineNumber()),
				event.getMessage() == null ? null : event.getMessage().getFormat());
	}

}
