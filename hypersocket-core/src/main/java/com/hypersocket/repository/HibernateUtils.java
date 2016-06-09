package com.hypersocket.repository;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.hypersocket.resource.Resource;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

public class HibernateUtils {

	static Logger log = LoggerFactory.getLogger(HibernateUtils.class);
	
	public static void configureSort(ColumnSort sort, Criteria criteria, Map<String,Criteria> assosications) {
		
		String column = sort.getColumn().getColumnName();
		if(!column.contains(".")) {
			criteria.addOrder(sort.getSort() == Sort.ASC ? 
					Order.asc(sort.getColumn().getColumnName()) :
					Order.desc(sort.getColumn().getColumnName()));
		} else {
			String[] elements = column.split("\\.");
			for(int i=0;i<elements.length-1;i++) {
				if(!assosications.containsKey(elements[i])) {
					assosications.put(elements[i], criteria.createCriteria(elements[i]));
				}
				criteria = assosications.get(elements[i]);
			}
			criteria.addOrder(sort.getSort() == Sort.ASC ? 
					Order.asc(elements[elements.length-1]) :
					Order.desc(elements[elements.length-1]));
		}
		
	}

	public static void configureSearch(String searchColumn, 
			String searchPattern, 
			Criteria criteria, 
			Class<?> clz,
			Map<String,Criteria> assosications) {
		
		if(StringUtils.isBlank(searchPattern)) {
			return;
		}
		if(StringUtils.isBlank(searchColumn)) {
			searchColumn = "name";
		}
		
		if(searchColumn.contains(".")) {
			String[] elements = searchColumn.split("\\.");
			for(int i=0;i<elements.length-1;i++) {
				Method m =  ReflectionUtils.findMethod(clz, "get" + StringUtils.capitalize(elements[i]));
				if(m==null) {
					log.error(String.format("Cannot find bean get method for %s on %s", elements[i], clz.getName()));
					return;
				}
				if(!assosications.containsKey(elements[i])) {
					assosications.put(elements[i], criteria.createCriteria(elements[i]));
				}
				criteria = assosications.get(elements[i]);
				clz = m.getReturnType();
			}
			searchColumn = elements[elements.length-1];
		}
		Method method = ReflectionUtils.findMethod(clz, "get" + StringUtils.capitalize(searchColumn));
		if(method==null) {
			if(log.isErrorEnabled()) {
				log.error("Cannot find method for search column value " + searchColumn);
			}
			return;
		}
		if(method.getReturnType().equals(String.class)) {	
			searchPattern = searchPattern.replace('*', '%');
			if(!searchPattern.contains("%")) {
				searchPattern = String.format("%%%s%%", searchPattern);
			}
			criteria.add(Restrictions.ilike(searchColumn, searchPattern));
			return;
		} 
		
		try {
			if(method.getReturnType().equals(Integer.class)) {
				criteria.add(Restrictions.eq(searchColumn, Integer.parseInt(searchPattern)));
			} else if(method.getReturnType().equals(Long.class)) {
				criteria.add(Restrictions.eq(searchColumn, Long.parseLong(searchPattern)));
			} else if(method.getReturnType().equals(Double.class)) {
				criteria.add(Restrictions.eq(searchColumn, Double.parseDouble(searchPattern)));
			} else if(method.getReturnType().equals(Boolean.class)) {
				criteria.add(Restrictions.eq(searchColumn, Boolean.parseBoolean(searchPattern)));
			} else if(method.getReturnType().equals(Date.class)) {
				/**
				 * TODO more complex
				 */
			} else if(Enum.class.isAssignableFrom(method.getReturnType())) {
				
				String[] matchValues = searchPattern.split(",");
				Class<?> enumType = method.getReturnType();
				Method valuesMethod = ReflectionUtils.findMethod(enumType, "values");
				Enum<?>[] values = (Enum<?>[]) valuesMethod.invoke(null);
		
				Set<Enum<?>> searchValues = new HashSet<Enum<?>>();
				for(Enum<?> e : values) {
					for(String matchValue : matchValues) {
						if(e.name().startsWith(matchValue)) {
							searchValues.add(e);
						}
					}
				}
				
				if(searchValues.size() > 0) {
					criteria.add(Restrictions.in(searchColumn, searchValues));
				}
			}
		
		} catch(Throwable t) {
			log.error(String.format("Failed to parse search string %s for column %s", searchPattern, searchColumn));
		}
	}

	public static Collection<Long> getResourceIds(Collection<? extends Resource> resources) {
		List<Long> ids = new ArrayList<Long>();
		for(Resource r : resources) {
			ids.add(r.getId());
		}
		return ids;
	}
	
	public static boolean isNotWildcard(String searchPattern) {
		switch(searchPattern) {
		case "*":
		case "%":
			return false;
		default:
			return true;
		}
	}

}
