package com.hypersocket.context;

import java.util.Locale;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.session.Session;
import com.hypersocket.session.SessionService;

@Service
public class ContextAnnotationServiceImpl implements ContextAnnotationService {

	static Logger log = LoggerFactory.getLogger(ContextAnnotationServiceImpl.class);
	
	@Autowired
	AuthenticationService authenticationService; 
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	SessionService sessionService; 
	
	@Autowired
	ConfigurationService configurationService;
	
	@Override
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		
		Session session = sessionService.getSystemSession();
		authenticationService.setCurrentSession(session, Locale.getDefault());
		
		try {
			return pjp.proceed();
		} catch(Throwable e) { 
			log.error("Exception thrown from context annotated method", e);
			throw e;
		} finally {
			authenticationService.clearPrincipalContext();
		}
	}
}
