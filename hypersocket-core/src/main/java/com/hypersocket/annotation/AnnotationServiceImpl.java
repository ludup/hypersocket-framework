/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.annotation;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class AnnotationServiceImpl implements AnnotationService {

	private Map<String,HypersocketExtensionPoint> extensionPoints = new HashMap<String,HypersocketExtensionPoint>();
	
	@Override
	public void registerExtensionPoint(String id, HypersocketExtensionPoint ext) {
		extensionPoints.put(id, ext);
	}
	
	@Override
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		String id = sig.getDeclaringType().getName() + "/" + sig.getName();
		
		
		if(extensionPoints.containsKey(id)) {
			HypersocketExtensionPoint ext = extensionPoints.get(id);
			if(ext.isExtending(pjp)) {
				return extensionPoints.get(id).invoke(pjp);
			}
		} 
		
		return pjp.proceed();
		
	}

	@Override
	public void deregisterExtensionPoint(String id) {
		extensionPoints.remove(id);
	}

	
}
