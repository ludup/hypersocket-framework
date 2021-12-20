package com.hypersocket.netty.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.slf4j.event.EventConstants;
import org.slf4j.event.Level;

public class UIAppender extends AbstractAppender {
	
	public interface Listener {
		void logEvent(UILogEvent event);
	}

	public static class UILogEvent {
		private long timestamp;
		private String text;
		private Level level;
		private int syslogLevel;
		private String logger;
		private String className;
		private String fileName;
		private String methodName;
		private int lineNumber;
		private String rawText;

		public UILogEvent(String text, LogEvent evt) {
			this.text = text;
			rawText = evt.getMessage().getFormat();
			timestamp = evt.getTimeMillis();
			switch(evt.getLevel().intLevel()) {
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
			logger = evt.getLoggerName();
			className = evt.getSource().getClassName();
			fileName = evt.getSource().getFileName();
			methodName = evt.getSource().getMethodName();
			lineNumber = evt.getSource().getLineNumber();
		}
		
		public int getSyslogLevel() {
			return syslogLevel;
		}

		public String getLogger() {
			return logger;
		}

		public String getClassName() {
			return className;
		}

		public String getFileName() {
			return fileName;
		}

		public String getMethodName() {
			return methodName;
		}

		public String getLineNumber() {
			return String.valueOf(lineNumber);
		}

		public Level getLevel() {
			return level;
		}

		public String getText() {
			return text;
		}

		public String getRawText() {
			return rawText;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}

	private static final int MAX_BUFFER_SIZE = 100;
	private static List<UILogEvent> buffer = Collections.synchronizedList(new LinkedList<>());
	private ExecutorService queue = Executors.newSingleThreadExecutor();
	private static UIAppender instance;
	private List<Listener> listeners=  new ArrayList<>();

	public UIAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions,
			Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}

	{
		instance = this;
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	public static UIAppender getInstance() {
		return instance;
	}

	@Override
	public void append(LogEvent event) {
		queue.execute(() -> {
			UILogEvent uiLogEvent = toUILogEvent(event);
			buffer.add(uiLogEvent);
			for(int i = listeners.size() -1 ; i >= 0 ; i--) {
				listeners.get(i).logEvent(uiLogEvent);
			}
			while (buffer.size() > MAX_BUFFER_SIZE) {
				buffer.remove(0);
			}
		});
	}

	private UILogEvent toUILogEvent(LogEvent event) {
		return new UILogEvent((String)toSerializable(event), event);
	}

	public static List<UILogEvent> getLoggingEventBuffer() {
		return buffer;
	}

}
