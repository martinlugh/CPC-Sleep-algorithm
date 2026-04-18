package com.hrv.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hrv.core.RRi;

/** 非平稳 HRV 分析。 */
public final class Nonstationary {
    private Nonstationary() {
    }

    public static TimeVarying timeVarying(double[] rri, double segSize, double overlap, boolean keepLast) {
        RRi rr = new RRi(rri);
        List<RRi> segments = rr.timeSplit(segSize, overlap, keepLast);
        List<Map<String, Double>> rows = new ArrayList<>();
        for (RRi seg : segments) {
            rows.add(Classical.timeDomain(seg.values()));
        }
        return new TimeVarying(rr, rows, segments, segSize, overlap);
    }
}
