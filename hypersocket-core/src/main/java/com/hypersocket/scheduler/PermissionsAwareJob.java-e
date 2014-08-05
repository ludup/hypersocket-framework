package com.hypersocket.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.RealmService;

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
	
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		try {
			authenticationService.setCurrentPrincipal(
							realmService.getSystemPrincipal(), 
							i18nService.getDefaultLocale(), 
							realmService.getSystemPrincipal().getRealm());
			
			try {
				executeJob(context);
			} finally {
				authenticationService.clearPrincipalContext();
			}
			
			
		} catch (Exception e) {
			log.error("Could not initialize AuthenticatedService instances of this job", e);
		} 
		

	}

	protected abstract void executeJob(JobExecutionContext context)
			throws JobExecutionException;
}
