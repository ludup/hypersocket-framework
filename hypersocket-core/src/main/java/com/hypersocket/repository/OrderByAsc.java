/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

public class OrderByAsc implements CriteriaConfiguration {

	private String property;
	
	public OrderByAsc(String property) {
		this.property = property;
	}
	@Override
	public void configure(Criteria criteria) {
		criteria.addOrder(Order.asc(property).ignoreCase());
	}
}
