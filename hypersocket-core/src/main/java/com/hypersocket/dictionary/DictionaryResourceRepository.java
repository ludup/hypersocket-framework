package com.hypersocket.dictionary;

import java.util.List;
import java.util.Locale;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractSimpleResourceRepository;
import com.hypersocket.tables.ColumnSort;

public interface DictionaryResourceRepository extends
	AbstractSimpleResourceRepository<Word> {

	void setup();

	String randomWord(Locale locale);

	boolean containsWord(Locale locale, String word, boolean caseInsenstive, boolean fallbackToDefault);

	List<Word> search(Locale locale, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs);

	long getResourceCount(Locale locale, String searchColumn, String searchPattern, CriteriaConfiguration... configs);

	void deleteResources(List<Long> wordIds);
}
