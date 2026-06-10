package com.digital.community.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.digital.community.vo.ImageGroupVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class ImageGroupListTypeHandler extends AbstractJsonTypeHandler<List<ImageGroupVO>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected List<ImageGroupVO> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ImageGroupVO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    protected String toJson(List<ImageGroupVO> obj) {
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
