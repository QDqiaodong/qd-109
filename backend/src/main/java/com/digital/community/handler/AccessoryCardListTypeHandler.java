package com.digital.community.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.digital.community.vo.AccessoryCardVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class AccessoryCardListTypeHandler extends AbstractJsonTypeHandler<List<AccessoryCardVO>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected List<AccessoryCardVO> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AccessoryCardVO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    protected String toJson(List<AccessoryCardVO> obj) {
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
