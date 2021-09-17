package com.hypersocket.email;

import org.simplejavamail.api.mailer.Mailer;

import com.hypersocket.config.ConfigurationChangedEvent;
import com.hypersocket.realm.Realm;

public interface MailerService {

	Mailer getMailer(Realm realm);

	String getSMTPValue(Realm realm, String name);

	int getSMTPIntValue(Realm realm, String name);

	String getSMTPDecryptedValue(Realm realm, String name);

	void onConfigurationChange(ConfigurationChangedEvent evt);
}
