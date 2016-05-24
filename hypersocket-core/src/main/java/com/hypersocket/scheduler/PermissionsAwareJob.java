package com.hypersocket.scheduler;

import java.util.Locale;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public abstract class PermissionsAwareJob extends TransactionalJob {

	static Logger log = LoggerFactory.getLogger(PermissionsAwareJob.class);

	@Autowired
	RealmService realmService;

	@Autowired
	RealmRepository realmRepository;
	
	@Autowired
	I18NService i18nService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	SessionService sessionService;
	
	@Override
	public void onExecute(JobExecutionContext context) {

		Realm realm = realmService.getSystemRealm();
		Principal principal = realmService.getSystemPrincipal();
		Locale locale = Locale.getDefault();
				
		try {

			if (context.getTrigger().getJobDataMap() instanceof PermissionsAwareJobData) {
				PermissionsAwareJobData data = (PermissionsAwareJobData) context
						.getTrigger().getJobDataMap();
				realm = realmRepository.getRealmById((Long) data.get("realm"));
				if (data.getCurrentPrincipal() != null) {
					principal = data.getCurrentPrincipal();
				}
				if(data.getLocale() != null) {
					locale = data.getLocale();
				}
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Executing permissions aware job as "
						+ realm.getName() + "/" + principal.getName());
			}

			authenticationService.setCurrentSession(sessionService.getSystemSession(), realm, principal, locale);

			try {
				executeJob(context);
			} finally {
				authenticationService.clearPrincipalContext();
			}

		} catch (Exception e) {
			log.error("Error in job", e);
			throw new IllegalStateException(e);
		}
	}
	
	protected Session getCurrentSession() {
		return authenticationService.getCurrentSession();
	}
	
	protected Principal getCurrentPrincipal() {
		return authenticationService.getCurrentPrincipal();
	}
	
	protected Realm getCurrentRealm() {
		return authenticationService.getCurrentRealm();
	}

	protected abstract void executeJob(JobExecutionContext context)
			throws JobExecutionException;
	
	protected void onTransactionComplete() {
		
	}

	protected void onTransactionFailure(Throwable t) {
		
	}
}
