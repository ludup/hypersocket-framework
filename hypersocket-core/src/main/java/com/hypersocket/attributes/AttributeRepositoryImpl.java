package com.hypersocket.attributes;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
import com.hypersocket.repository.AbstractRepositoryImpl;

@Repository
@Transactional
public class AttributeRepositoryImpl extends
		AbstractRepositoryImpl<Long> implements AttributeRepository {

	@Override
	public void saveCategory(AttributeCategory cat) {
		save(cat);
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes", 
				"attributeCategory" + String.valueOf(cat.getId()) + ".label",
				"",
				cat.getName()));
		
		try {
			I18N.flushOverrides();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void saveAttribute(Attribute attr) {
		save(attr);
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes", 
				"attribute" + String.valueOf(attr.getId()),
				"",
				attr.getName()));
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes", 
				"attribute" + String.valueOf(attr.getId()) + ".info",
				"",
				attr.getDescription()));
		
		try {
			I18N.flushOverrides();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
