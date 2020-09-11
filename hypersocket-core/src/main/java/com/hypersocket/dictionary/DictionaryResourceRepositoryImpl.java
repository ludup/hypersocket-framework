package com.hypersocket.dictionary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.resource.AbstractSimpleResourceRepositoryImpl;
import com.hypersocket.tables.ColumnSort;

@Repository
public class DictionaryResourceRepositoryImpl extends AbstractSimpleResourceRepositoryImpl<Word>
		implements DictionaryResourceRepository {

	/* Match hibernate.jdbc.batch_size */
	static final int BATCH_SIZE = 50;
	
	@Override
	protected Class<Word> getResourceClass() {
		return Word.class;
	}

	final static Log log = LogFactory.getLog(DictionaryResourceRepositoryImpl.class);

	private Map<Locale, Long> wordCounts = new HashMap<Locale, Long>();

	@Override
	@Transactional
	public void setup() {
		for (Locale locale : new Locale[] { Locale.getDefault() }) {
			try {
				if (log.isInfoEnabled()) {
					log.info("Setting up dictionary locale " + locale);
				}
				countWords(locale);
			} catch (RuntimeException rte) {
				if (rte.getCause() instanceof FileNotFoundException) {
					// Ignore
				} else {
					throw rte;
				}
			}
		}
	}

	@Override
	public void deleteResources(List<Long> wordIds) {
		sessionFactory.getCurrentSession().createQuery("delete from Word where id in :id")
				.setParameterList("id", wordIds).executeUpdate();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Word> search(Locale locale, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting, CriteriaConfiguration... configs) {
		if (searchColumn.equals(""))
			searchColumn = "text";
		return search(getResourceClass(), searchColumn, searchPattern, start, length, sorting,
				ArrayUtils.addAll(configs, new DeletedCriteria(false), new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						if (locale != null)
							criteria.add(Restrictions.eq("locale", locale));
					}
				}));
	}

	@Override
	@Transactional(readOnly = true)
	public long getResourceCount(Locale locale, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs) {
		if (searchColumn.equals(""))
			searchColumn = "text";
		return getCount(getResourceClass(), searchColumn, searchPattern,
				ArrayUtils.addAll(configs, new DeletedCriteria(false), new CriteriaConfiguration() {
					@Override
					public void configure(Criteria criteria) {
						if (locale != null)
							criteria.add(Restrictions.eq("locale", locale));
					}
				}));
	}

	@Override
	public String randomWord(Locale locale) {
		long w = countWords(locale);
		long randomIndex = (long) (Math.random() * (double) w);
		return get("wordIndex", randomIndex, Word.class, new LocaleCriteriaConfiguration(locale, false)).getText();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean containsWord(Locale locale, String word, boolean caseInsensitive, boolean fallbackToDefault) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Word.class);
		if (caseInsensitive) {
			criteria.add(Restrictions.eq("text", word).ignoreCase());
		} else {
			criteria.add(Restrictions.eq("text", word));
		}
		if (locale != null) {
			Disjunction dj = Restrictions.disjunction();
			dj.add(Restrictions.eq("locale", locale.toLanguageTag()));
			dj.add(Restrictions.isNull("locale"));
			criteria.add(dj);
		}
		return criteria.list().size() > 0;
	}

	private long loadDictionary(Locale locale) {
		try {
			if (log.isInfoEnabled()) {
				log.info("Loading dictionary for " + locale);
			}
			Set<Locale> locales = new HashSet<Locale>();
			locales.add(locale);

			InputStream resourceAsStream = getClass().getResourceAsStream("/dict/" + locale.toString());
			if (resourceAsStream == null) {
				resourceAsStream = getClass().getResourceAsStream("/dict/" + locale.getLanguage());
				if (resourceAsStream == null) {
					throw new FileNotFoundException();
				}
				locales.add(Locale.forLanguageTag(locale.getLanguage()));
			}
			BufferedReader r = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));
			String line;
			long index = 0;
			while ((line = r.readLine()) != null) {
				for (Locale l : locales) {
					Word w = new Word(l, line, index++);
					save(w);
					if (index % BATCH_SIZE == 0) {
						flush();
					}
				}
			}
			flush();
			return index;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	private long countWords(Locale locale) {
		synchronized (wordCounts) {
			if (wordCounts.containsKey(locale)) {
				return wordCounts.get(locale);
			}
		}
		Long wordCount = getCount(Word.class, new LocaleCriteriaConfiguration(locale, true));
		if (wordCount == 0) {
			wordCount = loadDictionary(locale);
		}
		wordCounts.put(locale, wordCount);
		return wordCount;
	}

	class LocaleCriteriaConfiguration implements CriteriaConfiguration {

		Locale locale;
		boolean localeOrNull;

		LocaleCriteriaConfiguration(Locale locale, boolean localeOrNull) {
			this.locale = locale;
			this.localeOrNull = localeOrNull;
		}

		@Override
		public void configure(Criteria criteria) {
			if (localeOrNull) {
				Disjunction dj = Restrictions.disjunction();
				dj.add(Restrictions.isNull("locale"));
				if (locale != null)
					dj.add(Restrictions.eq("locale", locale.toLanguageTag()));
				criteria.add(dj);
			} else {
				if (locale == null)
					criteria.add(Restrictions.isNull("locale"));
				else
					criteria.add(Restrictions.eq("locale", locale.toLanguageTag()));
			}
		}

	}

	@Override
	public void deleteRealm(Realm realm) {
	}

	@Override
	public boolean isDeletable() {
		return false;
	}
}
