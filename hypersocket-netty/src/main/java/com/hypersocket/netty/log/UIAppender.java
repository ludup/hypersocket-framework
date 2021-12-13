package com.hypersocket.netty.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.event.Level;

public class UIAppender extends AppenderSkeleton {
	
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
		private String lineNumber;
		private String rawText;

		public UILogEvent(String text, LoggingEvent evt) {
			this.text = text;
			rawText = evt.getRenderedMessage();
			timestamp = evt.timeStamp;
			syslogLevel = evt.getLevel().getSyslogEquivalent();
			switch(evt.getLevel().toInt()) {
			case org.apache.log4j.Level.ALL_INT:
			case org.apache.log4j.Level.TRACE_INT:
				level = Level.TRACE;
				break;
			case org.apache.log4j.Level.DEBUG_INT:
				level = Level.DEBUG;
				break;
			case org.apache.log4j.Level.ERROR_INT:
			case org.apache.log4j.Level.FATAL_INT:
			case org.apache.log4j.Level.OFF_INT:
				level = Level.ERROR;
				break;
			case org.apache.log4j.Level.WARN_INT:
				level = Level.WARN;
				break;
			default:
				level = Level.INFO;
				break;
			}
			logger = evt.getLoggerName();
			className = evt.getLocationInformation().getClassName();
			fileName = evt.getLocationInformation().getFileName();
			methodName = evt.getLocationInformation().getMethodName();
			lineNumber = evt.getLocationInformation().getLineNumber();
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
			return lineNumber;
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

	public UIAppender() {
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

	public UIAppender(Layout layout) {
		setLayout(layout);
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
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

	private UILogEvent toUILogEvent(LoggingEvent event) {
		return new UILogEvent(layout.format(event), event);
	}

	public static List<UILogEvent> getLoggingEventBuffer() {
		return buffer;
	}

}
