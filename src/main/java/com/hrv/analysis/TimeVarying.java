package com.hrv.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hrv.core.HrvUtils;
import com.hrv.core.RRi;

/** 时变指标结果对象。 */
public class TimeVarying {
    private final RRi rri;
    private final List<Map<String, Double>> results;
    private final List<RRi> segments;
    private final double segSize;
    private final double overlap;

    public TimeVarying(RRi rri, List<Map<String, Double>> results, List<RRi> segments, double segSize, double overlap) {
        this.rri = rri;
        this.results = results;
        this.segments = segments;
        this.segSize = segSize;
        this.overlap = overlap;
    }

    public List<Double> index(String key) {
        List<Double> out = new ArrayList<>();
        for (Map<String, Double> row : results) {
            if (!row.containsKey(key)) throw new IllegalArgumentException("不存在指标: " + key);
            out.add(row.get(key));
        }
        return out;
    }

    public List<Double> buildXAxis() {
        List<Double> out = new ArrayList<>();
        for (RRi s : segments) {
            out.add(HrvUtils.median(s.time()));
        }
        return out;
    }

    public List<Map<String, Double>> getResults() { return results; }
    public RRi getRri() { return rri; }
    public double getSegSize() { return segSize; }
    public double getOverlap() { return overlap; }
}
