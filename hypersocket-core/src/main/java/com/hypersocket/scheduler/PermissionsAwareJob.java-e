package com.hypersocket.scheduler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.RealmService;

public abstract class PermissionsAwareJob implements Job {

	static Logger log = LoggerFactory.getLogger(PermissionsAwareJob.class);
	
	@Autowired
	RealmService realmService;

	@Autowired
	I18NService i18nService;

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		try {
			List<AuthenticatedService> services = new ArrayList<AuthenticatedService>();
			for (Field field : getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Object inst = field.get(this);
				if (inst instanceof AuthenticatedService) {
					if(log.isInfoEnabled()) {
						log.info("Found AuthenticatedService " + inst.getClass().getSimpleName());
					}
					AuthenticatedService service = (AuthenticatedService) inst;
					service.setCurrentPrincipal(realmService
							.getSystemPrincipal(), i18nService
							.getDefaultLocale(), realmService
							.getSystemPrincipal().getRealm());
					services.add(service);
				}
			}
			
			try {
				executeJob(context);
			} finally {
				for(AuthenticatedService service : services) {
					service.clearPrincipalContext();
				}
			}
			
			
		} catch (Exception e) {
			log.error("Could not initialize AuthenticatedService instances of this job", e);
		} 
		

	}

	protected abstract void executeJob(JobExecutionContext context)
			throws JobExecutionException;
}
