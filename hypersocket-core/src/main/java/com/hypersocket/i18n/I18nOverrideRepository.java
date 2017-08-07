package com.hypersocket.i18n;

import java.util.Collection;
import java.util.Locale;

import com.hypersocket.repository.AbstractEntityRepository;

public interface I18nOverrideRepository extends AbstractEntityRepository<I18nOverride, Long> {

	I18nOverride getResource(Locale locale, String resourceBundle, String key);

	void deleteResource(Locale locale, String bundle, String id);

	void createResource(Locale locale, String bundle, String id, String translated);

	boolean hasResources(Locale locale, String resourceBundle);

	Collection<? extends String> getResourceKeys(Locale locale, String resourceBundle);

}
