package com.hypersocket.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.ConfigurationValueChangedEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;

@Service
public class MailerServiceImpl implements MailerService {

	public final static String SMTP_ENABLED = "smtp.enabled";
	public final static String SMTP_HOST = "smtp.host";
	public final static String SMTP_PORT = "smtp.port";
	public final static String SMTP_PROTOCOL = "smtp.protocol";
	public final static String SMTP_USERNAME = "smtp.username";
	public final static String SMTP_SESSION_TIMEOUT = "smtp.sessionTimeout";
	public final static String SMTP_PASSWORD = "smtp.password";
	public final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	public final static String SMTP_DEBUG = "smtp.debug";
	public final static String SMTP_FROM_NAME = "smtp.fromName";
	public final static String SMTP_REPLY_ADDRESS = "smtp.replyAddress";
	public final static String SMTP_REPLY_NAME = "smtp.replyName";
	public final static String SMTP_CONNECTION_POOL = "";
	private static final String SMTP_CONNECTION_POOL_WAIT_TIME = "smtp.connectionPoolWaitTime";
	private static final String SMTP_CONNECTION_POOL_SIZE = "smtp.connectionPoolSize";
	private static final String SMTP_CONNECTION_POOL_EXPIRY = "smtp.connectionPoolExpiry";
	private static final String SMTP_CONNECTION_POOL_MAX_SIZE = "smtp.connectionPoolMaxSize";
	
	Map<String,Mailer> cachedMailers = new HashMap<>();
	
	@Autowired
	private ConfigurationService configurationService; 
	
	@Autowired
	private RealmService realmService;
	
	static Logger log = LoggerFactory.getLogger(MailerServiceImpl.class);
	
	@Override
	public Mailer getMailer(Realm realm) {
		
		Mailer mailer = cachedMailers.get(realm.getUuid());
		if(Objects.nonNull(mailer)) {
			return mailer;
		}
		
		mailer = createMailer(realm);
		cachedMailers.put(realm.getUuid(), mailer);
		return mailer;
	}
	
	@Override
	@EventListener
	public void onConfigurationChange(ConfigurationValueChangedEvent evt) {
		
		if(evt.isSuccess()) {
			if(evt.getConfigResourceKey().startsWith("smtp.")) {
				if(log.isInfoEnabled()) {
					log.info("Resetting cached mailer because of SMTP configuration change");
				}
				Mailer mailer = cachedMailers.remove(evt.getCurrentRealm().getUuid());
				if(Objects.nonNull(mailer)) {
					mailer.shutdownConnectionPool();
				}
			}
		}
	}
	
	
	private Mailer createMailer(Realm realm) {
		
		
		Mailer mail;
		Properties props = new Properties();
		props.setProperty("mail.smtp.ssl.trust", "*");
		
		if(StringUtils.isNotBlank(getSMTPValue(realm, SMTP_USERNAME))) {
			mail = MailerBuilder.withSMTPServer(getSMTPValue(realm, SMTP_HOST), getSMTPIntValue(realm, SMTP_PORT))
				.withSMTPServerUsername(getSMTPValue(realm, SMTP_USERNAME))
				.withSMTPServerPassword(getSMTPDecryptedValue(realm, SMTP_PASSWORD))
				.withTransportStrategy(TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)])
				.withConnectionPoolClaimTimeoutMillis(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_WAIT_TIME) * 1000)
				.withConnectionPoolCoreSize(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_SIZE))
				.withConnectionPoolExpireAfterMillis(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_EXPIRY) * 1000)
				.withConnectionPoolMaxSize(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_MAX_SIZE))
				.withProperties(props)
				.withDebugLogging("true".equals(getSMTPValue(realm, SMTP_DEBUG)))
			    .trustingAllHosts(true)
			    .async()
				.withSessionTimeout(getSMTPIntValue(realm, SMTP_SESSION_TIMEOUT) * 1000)
				.buildMailer();
		} else {
			mail = MailerBuilder.withSMTPServer(getSMTPValue(realm, SMTP_HOST), getSMTPIntValue(realm, SMTP_PORT))
					.withTransportStrategy(TransportStrategy.values()[getSMTPIntValue(realm, SMTP_PROTOCOL)])
					.withConnectionPoolClaimTimeoutMillis(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_WAIT_TIME) * 1000)
					.withConnectionPoolCoreSize(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_SIZE))
					.withConnectionPoolExpireAfterMillis(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_EXPIRY) * 1000)
					.withConnectionPoolMaxSize(getSMTPIntValue(realm, SMTP_CONNECTION_POOL_MAX_SIZE))
					.withProperties(props)
				    .trustingAllHosts(true)
					.withDebugLogging("true".equals(getSMTPValue(realm, SMTP_DEBUG)))
				    .async()
					.withSessionTimeout(getSMTPIntValue(realm, SMTP_SESSION_TIMEOUT) * 1000)
					.buildMailer();
		}
		

		return mail;
	}
	
	@Override
	public String getSMTPValue(Realm realm, String name) {
		Realm systemRealm = realmService.getSystemRealm();
		if(!configurationService.getBooleanValue(realm, SMTP_ENABLED)) {
			realm = systemRealm;
		}
		return configurationService.getValue(realm, name);
	}
	
	@Override
	public int getSMTPIntValue(Realm realm, String name) {
		Realm systemRealm = realmService.getSystemRealm();
		if(!configurationService.getBooleanValue(realm, SMTP_ENABLED)) {
			realm = systemRealm;
		}
		return configurationService.getIntValue(realm, name);
	}
	
	@Override
	public String getSMTPDecryptedValue(Realm realm, String name) {
		Realm systemRealm = realmService.getSystemRealm();
		if(!configurationService.getBooleanValue(realm, SMTP_ENABLED)) {
			realm = systemRealm;
		}
		return configurationService.getDecryptedValue(realm, name);
	}
}
