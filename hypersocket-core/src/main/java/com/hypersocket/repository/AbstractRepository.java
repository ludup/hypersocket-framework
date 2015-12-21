/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import java.util.List;

import com.hypersocket.tables.Sort;

public interface AbstractRepository<K> {

	public void refresh(Object entity);
	
	public void flush();

	Long getCount(Class<?> clz, String searchColumn, String searchPattern, CriteriaConfiguration... configs);

	Long getCount(Class<?> clz, CriteriaConfiguration... configs);

	Long max(String column, Class<?> clz, CriteriaConfiguration... configs);

	Long min(String column, Class<?> clz, CriteriaConfiguration... configs);

	List<?> sum(Class<?> clz, String groupBy, Sort order, CriteriaConfiguration... configs);

	List<?> getCounts(Class<?> clz, String groupBy,
			CriteriaConfiguration... configs);

	void evict(Object entity);

	List<?> getCounts(Class<?> clz, String groupBy, boolean highestFirst, int maxResults,
			CriteriaConfiguration... configs);

}
