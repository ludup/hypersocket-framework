/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.annotation;

import org.aspectj.lang.ProceedingJoinPoint;

public class AnnotationServiceImpl implements AnnotationService {

	public AnnotationServiceImpl() {
		
	}
	@Override
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		
		return pjp.proceed();
	}

}
