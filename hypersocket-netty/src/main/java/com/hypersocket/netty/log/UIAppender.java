package com.hypersocket.netty.log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

public class UIAppender extends AppenderSkeleton {

	public static class UILogEvent {
		private long timestamp = System.currentTimeMillis();
		private String text;

		public UILogEvent(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}

	private static final int MAX_BUFFER_SIZE = 100;
	private static List<UILogEvent> buffer = Collections.synchronizedList(new LinkedList<>());
	private ExecutorService queue = Executors.newSingleThreadExecutor();

	public UIAppender() {
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
			buffer.add(toUILogEvent(event));
			while (buffer.size() > MAX_BUFFER_SIZE) {
				buffer.remove(0);
			}
		});
	}

	private UILogEvent toUILogEvent(LoggingEvent event) {
		return new UILogEvent(layout.format(event));
	}

	public static List<UILogEvent> getLoggingEventBuffer() {
		return buffer;
	}

}
