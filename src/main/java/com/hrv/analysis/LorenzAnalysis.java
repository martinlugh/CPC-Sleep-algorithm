package com.hrv.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hrv.core.HrvUtils;

/**
 * 洛伦兹散点图（Poincaré plot）计算模块。
 */
public final class LorenzAnalysis {
    private LorenzAnalysis() {
    }

    /**
     * 根据 RRi（ms）构建洛伦兹散点与 SD1/SD2，语义与 Classical 保持一致。
     */
    public static LorenzResult analyze(double[] rri) {
        double[] x = HrvUtils.transformRri(rri);
        List<LorenzPoint> points = new ArrayList<>();
        for (int i = 0; i < x.length - 1; i++) {
            points.add(new LorenzPoint(x[i], x[i + 1]));
        }

        Map<String, Double> nl = Classical.nonLinear(x);
        return new LorenzResult(points, nl.get("sd1"), nl.get("sd2"));
    }
}
