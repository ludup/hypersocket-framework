/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.repository;

import java.util.Iterator;
import java.util.List;

import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

public interface AbstractRepository<K> {

	void refresh(Object entity);
	
	Object merge(Object entity);
	
	void flush();

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

	void assosicate(Object entity);

	<I> Iterator<I> iterate(Class<I> clazz, ColumnSort[] sorting, CriteriaConfiguration... configs);

	List<?> total(Class<?> clz, String column, Sort order, CriteriaConfiguration... configs);

	Long getDistinctCount(Class<?> clz, String groupBy, CriteriaConfiguration... configs);

	<T> List<T> search(Class<T> clz, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs);

	<T> long searchCount(Class<T> clz, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs);
}
