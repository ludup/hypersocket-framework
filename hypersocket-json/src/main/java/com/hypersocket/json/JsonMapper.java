package com.hypersocket.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonMapper {

    public final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectMapper get() {
        return objectMapper;
    }

}
