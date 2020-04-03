package com.hypersocket.json;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonMapper {

    public final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectMapper get() {
        return objectMapper;
    }

}
