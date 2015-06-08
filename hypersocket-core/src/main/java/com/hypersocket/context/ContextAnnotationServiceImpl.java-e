package com.hypersocket.context;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

public class ContextAnnotationServiceImpl implements ContextAnnotationService {

	@Autowired
	AuthenticationService authenticationService; 
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	SessionService sessionService; 
	
	@Autowired
	I18NService i18nService; 
	
	@Override
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		
		Session session = sessionService.getSystemSession();
		authenticationService.setCurrentSession(session, i18nService.getDefaultLocale());
		
		try {
			return pjp.proceed();
		} finally {
			authenticationService.clearPrincipalContext();
		}
	}
}
