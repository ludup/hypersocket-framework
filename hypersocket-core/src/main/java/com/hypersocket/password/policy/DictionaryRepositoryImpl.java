/* HEADER */
package com.hypersocket.password.policy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
public class DictionaryRepositoryImpl extends AbstractRepositoryImpl<Long> implements DictionaryRepository {

	final static Log log = LogFactory.getLog(DictionaryRepositoryImpl.class);

	private Map<Locale, Long> wordCounts = new HashMap<Locale, Long>();

	@Override
	@Transactional
	public void setup() {
      for (Locale locale : new Locale[] { Locale.getDefault() } ) {
			try {
				if(log.isInfoEnabled()) {
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
	public String randomWord(Locale locale) {
		long w = countWords(locale);
		long randomIndex = (long) (Math.random() * (double) w);	
		return get("wordIndex", randomIndex, Word.class, new LocaleCriteriaConfiguration(locale)).getText();
	}

	@Override
	@Transactional(readOnly=true)
	public boolean containsWord(Locale locale, String word) {
		return !list("text", word, Word.class, true, new LocaleCriteriaConfiguration(locale)).isEmpty();
	}

	private long loadDictionary(Locale locale) {
		try {
			if(log.isInfoEnabled()) {
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
				for(Locale l : locales) {
					Word w = new Word(l, line, index++);
					save(w);
					if (index % 1000 == 0) {
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
		Long wordCount = getCount(Word.class, new LocaleCriteriaConfiguration(locale));
		if (wordCount == 0) {
			wordCount = loadDictionary(locale);
		}
		wordCounts.put(locale, wordCount);
		return wordCount;
	}

	class LocaleCriteriaConfiguration implements CriteriaConfiguration {

		Locale locale;
		
		LocaleCriteriaConfiguration(Locale locale) {
			this.locale = locale;
		}
		
		@Override
		public void configure(Criteria criteria) {
			criteria.add(Restrictions.eq("locale", locale));
		}
		
	}
}