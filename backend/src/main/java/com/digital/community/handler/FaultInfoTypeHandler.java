package com.digital.community.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class FaultInfoTypeHandler extends AbstractJsonTypeHandler<Map<String, String>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Map<String, String> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    protected String toJson(Map<String, String> obj) {
        if (obj == null || obj.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
