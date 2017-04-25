package com.hypersocket.migration.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.hypersocket.migration.execution.stack.MigrationCurrentInfo;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import com.hypersocket.repository.AbstractEntity;

import java.io.IOException;
import java.util.Collection;

public class MigrationBeanDeserializer extends BeanDeserializer {

	private static final long serialVersionUID = -8306775620484569378L;
	
	MigrationCurrentStack migrationCurrentStack;

    public MigrationBeanDeserializer(BeanDeserializerBase src, MigrationCurrentStack migrationCurrentStack) {
        super(src);
        this.migrationCurrentStack = migrationCurrentStack;
    }

    //below code is copied as it is from BeanDeserializer.deserialize
    //with our customization
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean) throws IOException {
        p.setCurrentValue(bean);
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        if (_unwrappedPropertyHandler != null) {
            return deserializeWithUnwrapped(p, ctxt, bean);
        }
        if (_externalTypeIdHandler != null) {
            return deserializeWithExternalTypeId(p, ctxt, bean);
        }
        String propName;

        // 23-Mar-2010, tatu: In some cases, we start with full JSON object too...
        if (p.isExpectedStartObjectToken()) {
            propName = p.nextFieldName();
            if (propName == null) {
                return bean;
            }
        } else {
            if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
                propName = p.getCurrentName();
            } else {
                return bean;
            }
        }
        if (_needViewProcesing) {
            Class<?> view = ctxt.getActiveView();
            if (view != null) {
                return deserializeWithView(p, ctxt, bean, view);
            }
        }


        do {
            p.nextToken();
            SettableBeanProperty prop = _beanProperties.find(propName);

            if (prop != null) { // normal case
                try {
                    MigrationCurrentInfo migrationCurrentInfo = new MigrationCurrentInfo(bean, propName);
                    migrationCurrentStack.addState(migrationCurrentInfo);

                    if (Collection.class.isAssignableFrom(prop.getType().getRawClass())
                            || AbstractEntity.class.isAssignableFrom(prop.getType().getRawClass())) {
                        //we need customization here
                        //was prop.deserializeAndSet(p, ctxt, bean);
                        //we did this as we do not want jackson to manipulate refernces
                        prop.deserialize(p, ctxt);
                    } else {
                        prop.deserializeAndSet(p, ctxt, bean);
                    }
                } catch (Exception e) {
                    wrapAndThrow(e, bean, propName, ctxt);
                } finally {
                    migrationCurrentStack.popState();
                }

                continue;
            }
            handleUnknownVanilla(p, ctxt, bean, propName);
        } while ((propName = p.nextFieldName()) != null);
        return bean;
    }
}
