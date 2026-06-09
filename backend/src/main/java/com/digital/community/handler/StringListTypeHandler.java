package com.digital.community.handler;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StringListTypeHandler extends AbstractJsonTypeHandler<List<String>> {

    @Override
    protected List<String> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(json.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    protected String toJson(List<String> obj) {
        if (obj == null || obj.isEmpty()) {
            return "";
        }
        return String.join(",", obj);
    }
}
