package com.hypersocket.scheduler;

import java.util.Locale;

import org.quartz.Job;
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
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.SessionService;

public abstract class PermissionsAwareJob implements Job {

	static Logger log = LoggerFactory.getLogger(PermissionsAwareJob.class);

	@Autowired
	RealmService realmService;

	@Autowired
	I18NService i18nService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	SessionService sessionService;
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		Realm realm = realmService.getSystemRealm();
		Principal principal = realmService.getSystemPrincipal();
		Locale locale = i18nService.getDefaultLocale();
		
		if (context.getTrigger().getJobDataMap() instanceof PermissionsAwareJobData) {
			PermissionsAwareJobData data = (PermissionsAwareJobData) context
					.getTrigger().getJobDataMap();
			realm = data.getCurrentRealm();
			if (data.getCurrentPrincipal() != null) {
				principal = data.getCurrentPrincipal();
			}
			if(data.getLocale() != null) {
				locale = data.getLocale();
			}
		}

		
		try {

			if (log.isInfoEnabled() && !principal.equals(realmService.getSystemPrincipal())) {
				log.info("Executing permissions aware job as "
						+ realm.getName() + "/" + principal.getName());
			}

			authenticationService.setCurrentSession(sessionService.getSystemSession(), 
					locale);
			authenticationService.setCurrentPrincipal(principal,
					locale, realm);

			try {
				executeJob(context);
			} finally {
				authenticationService.clearPrincipalContext();
			}

		} catch (Exception e) {
			log.error(
					"Could not initialize AuthenticatedService instances of this job",
					e);
		}

	}

	protected abstract void executeJob(JobExecutionContext context)
			throws JobExecutionException;
}
