package com.hypersocket.migration.exporter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hypersocket.repository.AbstractEntity;

import java.io.IOException;

public interface JsonPropertiesAdder {
    void add(AbstractEntity value, JsonGenerator gen, SerializerProvider provider) throws IOException;
}
