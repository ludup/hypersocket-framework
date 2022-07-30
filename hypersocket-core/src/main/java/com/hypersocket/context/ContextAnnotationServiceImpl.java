package com.hypersocket.context;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.realm.RealmService;

@Service
public class ContextAnnotationServiceImpl implements ContextAnnotationService {

	private final static Logger log = LoggerFactory.getLogger(ContextAnnotationServiceImpl.class);

	@Autowired
	private RealmService realmService;

	@Override
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		var sig = pjp.getSignature();
		if (sig instanceof MethodSignature) {
			var ms = (MethodSignature) sig;
			var m = ms.getMethod();
			var annot = m.getAnnotation(AuthenticatedContext.class);
			if (annot == null) {
				/**
				 * INFO BPS 30/07/22 - Check the implementation class for annotation too. AOP
				 * gives us the interface method above, but the implementation can be annotated
				 * too. So we look for a method with the same name and signature.
				 */
				var clz = pjp.getTarget().getClass();
				try {
					var mth = clz.getMethod(m.getName(), m.getParameterTypes());
					annot = mth.getAnnotation(AuthenticatedContext.class);
				} catch (Exception e) {
				}
			}
			if (annot != null) {
				if (annot.preferActive()) {
					throw new UnsupportedOperationException("Realm Host is not supported in services.");
				} else if (annot.system()) {
					try (var c = realmService.tryWithSystemContext()) {
						return pjp.proceed();
					} catch (Throwable e) {
						log.error("Exception thrown from context annotated method", e);
						throw e;
					}
				} else if (annot.realmHost()) {
					throw new UnsupportedOperationException("Realm Host is not supported in services.");
				} else if (annot.currentRealmOrDefault()) {
					throw new UnsupportedOperationException("Current realm or default is not supported in services.");
				} else {
					throw new UnsupportedOperationException("Active user is not supported in services.");
				}
			}
		}
		return pjp.proceed();
	}
}
