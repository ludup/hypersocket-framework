/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hypersocket.repository.CriteriaConfiguration;

public class HiddenFilter implements CriteriaConfiguration {

	boolean showHidden = false;
	
	public HiddenFilter() {
		
	}
	public HiddenFilter(boolean showHidden) {
		this.showHidden = showHidden;
	}
	
	@Override
	public void configure(Criteria criteria) {
		if(!showHidden) {
			criteria.add(Restrictions.eq("hidden", false));
		}

	}

}
