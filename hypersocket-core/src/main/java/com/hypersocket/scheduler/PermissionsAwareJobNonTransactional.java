package com.hypersocket.scheduler;

import java.util.Locale;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public abstract class PermissionsAwareJobNonTransactional implements Job, Runnable {

	static Logger log = LoggerFactory.getLogger(PermissionsAwareJobNonTransactional.class);

	@Autowired
	private RealmService realmService;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private RealmRepository realmRepository;

	private JobExecutionContext context;
	
	@Override
	public void execute(JobExecutionContext context) {
		this.context = context;
		sessionService.runAsSystemContext(this);
	}
	
	public void run() {
		
		Realm realm = realmService.getSystemRealm();
		Principal principal = realmService.getSystemPrincipal();
		Locale locale = Locale.getDefault();
		Session session = sessionService.getSystemSession();
		
		try {

			if (context.getTrigger().getJobDataMap().containsKey("permissions")) {
				JobDataMap data = context.getTrigger().getJobDataMap();
				if(data.containsKey("session")) {
					session = sessionService.getSession(data.getString("session"));
					realm = session.getCurrentRealm();
					principal = session.getCurrentPrincipal();
				}
				if(data.containsKey("realm")) {
					realm = realmRepository.getRealmById(data.getLong("realm"));
				}
				if(data.containsKey("principal")) {
					principal = realmService.getPrincipalById(realm, data.getLong("principal"), PrincipalType.USER);
				}
				if(data.containsKey("locale")) {
					locale = Locale.forLanguageTag(data.getString("locale"));
				}
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Executing permissions aware job as "
						+ realm.getName() + "/" + principal.getName());
			}

			try(var c = authenticationService.tryAs(session, realm, principal, locale)) {
				executeJob(context);
			} 
		} catch (Throwable e) {
			log.error("Error in job", e);
			throw new IllegalStateException(e.getMessage(), e);
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
	
}
