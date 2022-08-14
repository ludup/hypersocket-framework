package com.hypersocket.netty.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

@Plugin(name = "XYamlConfigurationFactory", category = "ConfigurationFactory")
@Order(10)
public class XYamlConfigurationFactory extends ConfigurationFactory {

	public static final String[] SUFFIXES = new String[] { ".yml", "*" };

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
        if (!isActive()) {
            return null;
        }
        return new XYamlConfiguration(loggerContext, source);
    }

	public String[] getSupportedTypes() {
		return SUFFIXES;
	}

	public static class XYamlConfiguration extends YamlConfiguration {
		public XYamlConfiguration(final LoggerContext loggerContext, ConfigurationSource configSource) {
			super(loggerContext, configSource);
		}

		@Override
		protected void doConfigure() {
			super.doConfigure();
			var context = (LoggerContext) LogManager.getContext(false);
			var config = context.getConfiguration();
			var rootLogger = config.getRootLogger();
			if(System.getProperty("hypersocket.log4j2") != null) {
				var lvl = Level.valueOf(System.getProperty("hypersocket.log4j2"));
				var loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
				loggerConfig.setLevel(lvl);
			}
			System.getProperties().forEach((k, v) -> {
				if(k.toString().startsWith("hypersocket.log4j2.")) {
					var namespace= k.toString().substring(19);
					@SuppressWarnings("deprecation")
					var loggerConfig = LoggerConfig.createLogger(false, Level.valueOf(v.toString()), namespace, "true",
							rootLogger.getAppenderRefs().toArray(new AppenderRef[0]), null, config, null);
					for(var ap : rootLogger.getAppenders().values()) {
						loggerConfig.addAppender(ap, null, null);
					}
					addLogger(namespace, loggerConfig);
				}
			});
		}
	}
}