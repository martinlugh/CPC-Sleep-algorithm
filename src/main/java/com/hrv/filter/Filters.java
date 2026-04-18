package com.hrv.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hrv.core.HrvUtils;
import com.hrv.core.RRi;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/** RRi 滤波器。 */
public final class Filters {
    private Filters() {
    }

    public static RRi quotient(double[] rawRri, double[] rawTime) {
        double[] rri = HrvUtils.transformRri(rawRri);
        double[] time = rawTime == null ? HrvUtils.createTimeInfo(rri) : Arrays.copyOf(rawTime, rawTime.length);

        List<Integer> toRemove = new ArrayList<>();
        int l = rri.length - 1;
        for (int i = 0; i < l - 1; i++) {
            boolean bad = (rri[i] / rri[i + 1] < 0.8)
                    || (rri[i] / rri[i + 1] > 1.2)
                    || (rri[i + 1] / rri[i] < 0.8)
                    || (rri[i + 1] / rri[i] > 1.2);
            if (bad) toRemove.add(i);
        }

        double[] fr = removeByIndex(rri, toRemove);
        double[] ft = removeByIndex(time, toRemove);
        return new RRi(fr, ft);
    }

    public static RRi movingAverage(double[] rawRri, double[] rawTime, int order) {
        return movingFunction(rawRri, rawTime, order, false);
    }

    public static RRi movingMedian(double[] rawRri, double[] rawTime, int order) {
        return movingFunction(rawRri, rawTime, order, true);
    }

    public static RRi thresholdFilter(double[] rawRri, double[] rawTime, Object threshold, int localMedianSize) {
        double[] rri = HrvUtils.transformRri(rawRri);
        double[] time = rawTime == null ? HrvUtils.createTimeInfo(rri) : Arrays.copyOf(rawTime, rawTime.length);

        Map<String, Integer> strength = Map.of(
                "very low", 450,
                "low", 350,
                "medium", 250,
                "strong", 150,
                "very strong", 50
        );
        double th;
        if (threshold instanceof String s) {
            th = strength.getOrDefault(s.toLowerCase(), 250);
        } else if (threshold instanceof Number n) {
            th = n.doubleValue();
        } else {
            th = 250;
        }

        List<Integer> bad = new ArrayList<>();
        for (int j = localMedianSize; j < rri.length; j++) {
            double med = HrvUtils.median(Arrays.copyOfRange(rri, j - localMedianSize, j));
            if (rri[j] > med + th) bad.add(j);
        }
        for (int j = 0; j < localMedianSize; j++) {
            List<Double> local = new ArrayList<>();
            for (int k = 0; k <= localMedianSize; k++) {
                if (k != j) local.add(rri[k]);
            }
            double med = HrvUtils.median(local.stream().mapToDouble(Double::doubleValue).toArray());
            if (Math.abs(rri[j] - med) > th) bad.add(j);
        }

        double[] keepRri = removeByIndex(rri, bad);
        double[] keepTime = removeByIndex(time, bad);
        PolynomialSplineFunction spline = new SplineInterpolator().interpolate(keepTime, keepRri);

        double[] fixed = new double[rri.length];
        for (int i = 0; i < fixed.length; i++) fixed[i] = spline.value(time[i]);
        return new RRi(fixed, time);
    }

    private static RRi movingFunction(double[] rawRri, double[] rawTime, int order, boolean medianMode) {
        double[] rri = HrvUtils.transformRri(rawRri);
        double[] time = rawTime == null ? HrvUtils.createTimeInfo(rri) : Arrays.copyOf(rawTime, rawTime.length);
        int offset = order / 2;
        double[] out = Arrays.copyOf(rri, rri.length);

        for (int i = offset; i < rri.length - offset; i++) {
            double[] win = Arrays.copyOfRange(rri, i - offset, i + offset + 1);
            out[i] = medianMode ? HrvUtils.median(win) : HrvUtils.mean(win);
        }
        return new RRi(out, time);
    }

    private static double[] removeByIndex(double[] arr, List<Integer> idx) {
        boolean[] bad = new boolean[arr.length];
        for (Integer i : idx) if (i >= 0 && i < bad.length) bad[i] = true;
        double[] out = new double[arr.length - (int) idx.stream().filter(i -> i >= 0 && i < arr.length).distinct().count()];
        int p = 0;
        for (int i = 0; i < arr.length; i++) {
            if (!bad[i]) out[p++] = arr[i];
        }
        return out;
    }
}
