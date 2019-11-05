package com.hypersocket.i18n;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractEntityRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
public class I18nOverrideRepositoryImpl extends AbstractEntityRepositoryImpl<I18nOverride, Long> implements I18nOverrideRepository {

	@Autowired
	I18NService i18nService;
	
	@Override
	protected Class<I18nOverride> getEntityClass() {
		return I18nOverride.class;
	}

	@Override
	@Transactional(readOnly=true)
	public I18nOverride getResource(final Locale locale, final String resourceBundle, final String key) {
		if(StringUtils.isBlank(resourceBundle) || StringUtils.isBlank(key)) {
			return null;
		}
		return get("name", key, I18nOverride.class, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.or(
						Restrictions.eq("locale", locale.toString()),
						Restrictions.eq("locale", locale.getLanguage())));
				criteria.add(Restrictions.eq("bundle", resourceBundle));
			}
		});
	}

	@Override
	@Transactional
	public void deleteResource(Locale locale, String bundle, String key) {
		I18nOverride o = getResource(locale, bundle, key);
		if(o!=null) {
			delete(o);
		}
		i18nService.clearCache(locale);
	}

	@Override
	@Transactional
	public void createResource(Locale locale, String bundle, String id, String translated) {
		
		
		I18nOverride o = getResource(locale, bundle, id);
		if(o==null) {
			o = new I18nOverride();
		}
		o.setLocale(locale.toString());
		o.setBundle(bundle);
		o.setName(id);
		o.setValue(translated);
		
		save(o);
		
		i18nService.clearCache(locale);
	}

	@Override
	@Transactional(readOnly=true)
	public boolean hasResources(final Locale locale, final String resourceBundle) {
		
		return getCount(I18nOverride.class, new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.or(
						Restrictions.eq("locale", locale.toString()),
						Restrictions.eq("locale", locale.getLanguage())));
				criteria.add(Restrictions.eq("bundle", resourceBundle));
			}
			
		}) > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public Collection<? extends String> getResourceKeys(Locale locale, String resourceBundle) {
		Criteria criteria = createCriteria(I18nOverride.class);
		criteria.add(Restrictions.or(
				Restrictions.eq("locale", locale.toString()),
				Restrictions.eq("locale", locale.getLanguage())));
		criteria.add(Restrictions.eq("bundle", resourceBundle));
		criteria.setProjection(Projections.property("name"));
		@SuppressWarnings("rawtypes")
		List results = criteria.list();
		return results;
	}

}
