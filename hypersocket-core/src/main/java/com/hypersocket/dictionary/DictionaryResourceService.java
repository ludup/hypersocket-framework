/*******************************************************************************
 * Copyright (c) 2019 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package com.hypersocket.dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

public interface DictionaryResourceService  {

	Word updateResource(Word resourceById, Locale locale, String name)
			throws ResourceException, AccessDeniedException;

	Word createResource(Locale locale, String name)
			throws ResourceException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(Word resource)
			throws AccessDeniedException;

	String randomWord(Locale locale);

	boolean containsWord(Locale locale, String word);

	void deleteResource(Word resource) throws AccessDeniedException, ResourceException;

	Word getResourceById(long id) throws ResourceNotFoundException;

	List<?> searchResources(Locale locale, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException;

	Long getResourceCount(Locale locale, String searchColumn, String searchPattern) throws AccessDeniedException;

	long importDictionary(Locale locale, Reader input, boolean ignoreDuplicates) throws ResourceException, IOException, AccessDeniedException;

	void deleteResources(List<Long> ids) throws AccessDeniedException, ResourceException;

}
