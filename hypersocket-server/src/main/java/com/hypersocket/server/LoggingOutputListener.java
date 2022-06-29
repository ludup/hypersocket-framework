package com.hypersocket.server;

import org.slf4j.event.Level;

public interface LoggingOutputListener {

	void logEvent(LoggingOutputEvent event);

	public static class LoggingOutputEvent {
		protected long timestamp;
		protected String text;
		protected Level level;
		protected int syslogLevel;
		protected String logger;
		protected String className;
		protected String fileName;
		protected String methodName;
		protected String lineNumber;
		protected String rawText;
		
		public LoggingOutputEvent() {
		}

		public LoggingOutputEvent(long timestamp, String text, Level level, int syslogLevel, String logger,
				String className, String fileName, String methodName, String lineNumber, String rawText) {
			super();
			this.timestamp = timestamp;
			this.text = text;
			this.level = level;
			this.syslogLevel = syslogLevel;
			this.logger = logger;
			this.className = className;
			this.fileName = fileName;
			this.methodName = methodName;
			this.lineNumber = lineNumber;
			this.rawText = rawText;
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
}
