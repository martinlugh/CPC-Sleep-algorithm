package com.hrv.core;

import java.util.LinkedHashMap;
import java.util.Map;

/** RRi 描述统计结果容器。 */
public class RRiDescription {
    private final Map<String, Map<String, Double>> values = new LinkedHashMap<>();

    public void put(String metric, String field, double value) {
        values.computeIfAbsent(metric, k -> new LinkedHashMap<>()).put(field, value);
    }

    public Map<String, Map<String, Double>> asMap() {
        return values;
    }
}
