package com.hypersocket.attributes;

import java.util.Locale;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.Message;
import com.hypersocket.repository.AbstractEntityRepositoryImpl;

@Repository
@Transactional
public class AttributeRepositoryImpl extends
		AbstractEntityRepositoryImpl<Attribute,Long> implements AttributeRepository {

	@Override
	public void saveCategory(AttributeCategory cat) {
		save(cat);
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes", 
				"attributeCategory" + String.valueOf(cat.getId()) + ".label",
				"",
				cat.getName()));
		I18N.flushOverrides();

	}
	
	@Override
	public void saveAttribute(Attribute attr) {
		saveEntity(attr);
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes", 
				"attribute" + String.valueOf(attr.getId()),
				"",
				attr.getName()));
		
		I18N.overrideMessage(Locale.ENGLISH, new Message("UserAttributes", 
				"attribute" + String.valueOf(attr.getId()) + ".info",
				"",
				attr.getDescription()));
		

		I18N.flushOverrides();
	}

	@Override
	protected Class<Attribute> getEntityClass() {
		return Attribute.class;
	}

}
