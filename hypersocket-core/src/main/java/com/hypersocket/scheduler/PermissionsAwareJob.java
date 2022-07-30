package com.hypersocket.scheduler;

import java.util.Locale;

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

public abstract class PermissionsAwareJob extends TransactionalJob {

	static Logger log = LoggerFactory.getLogger(PermissionsAwareJob.class);

	@Autowired
	private RealmService realmService;

	@Autowired
	private RealmRepository realmRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private SessionService sessionService;

	protected Realm realm;
	protected Principal principal;
	protected Locale locale;
	protected Session session;

	@Override
	public void onExecute(JobExecutionContext context) {
		sessionService.runAsSystemContext(() -> {
			realm = realmService.getSystemRealm();
			principal = realmService.getSystemPrincipal();
			locale = Locale.getDefault();
			session = sessionService.getSystemSession();

			try {

				if (context.getTrigger().getJobDataMap().containsKey("permissions")) {
					JobDataMap data = context.getTrigger().getJobDataMap();
					if (data.containsKey("session")) {
						session = sessionService.getSession(data.getString("session"));
						realm = session.getCurrentRealm();
						principal = session.getCurrentPrincipal();
					}

					if (data.containsKey("realm")) {
						Object obj = data.get("realm");
						if (obj instanceof Long) {
							realm = realmRepository.getRealmById(data.getLong("realm"));
						} else if (obj instanceof Realm) {
							realm = (Realm) obj;
						}

					}
					if (data.containsKey("principal")) {
						Object obj = data.get("principal");
						if (obj instanceof Long) {
							principal = realmService.getPrincipalById(realm, data.getLong("principal"),
									PrincipalType.USER);
						} else if (obj instanceof Principal) {
							principal = (Principal) obj;
						}
					}
					if (data.containsKey("locale")) {
						locale = Locale.forLanguageTag(data.getString("locale"));
					}
				}

				if (log.isDebugEnabled()) {
					log.debug("Executing permissions aware job as " + realm.getName() + "/" + principal.getName());
				}

				try (var c = authenticationService.tryAs(session, realm, principal, locale)) {
					executeJob(context);
				}

			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		});
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

	protected abstract void executeJob(JobExecutionContext context) throws JobExecutionException;

	protected void onTransactionComplete() {

	}

	protected void onTransactionFailure(Throwable t) {

	}
}
