package com.hrv.core;

import java.util.Arrays;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * HRV 公共工具函数（Java 版本）。
 */
public final class HrvUtils {
    private HrvUtils() {
    }

    /** 将 RRi 转换并校验为毫秒序列。 */
    public static double[] transformRri(double[] rri) {
        validatePositiveNumbers(rri);
        double[] out = Arrays.copyOf(rri, rri.length);
        // 与 Python 原实现保持一致：中位数 < 10 时视为“秒”并转毫秒
        if (median(out) < 10.0) {
            for (int i = 0; i < out.length; i++) {
                out[i] *= 1000.0;
            }
        }
        return out;
    }

    public static void validatePositiveNumbers(double[] rri) {
        for (double v : rri) {
            if (!(v > 0.0)) {
                throw new IllegalArgumentException("rri 必须全部为正数且非 0");
            }
        }
    }

    /** 根据 RRi 生成秒级时间轴。 */
    public static double[] createTimeInfo(double[] rriMillis) {
        double[] time = new double[rriMillis.length];
        double acc = 0.0;
        for (int i = 0; i < rriMillis.length; i++) {
            acc += rriMillis[i] / 1000.0;
            time[i] = acc;
        }
        double start = time[0];
        for (int i = 0; i < time.length; i++) {
            time[i] -= start;
        }
        return time;
    }

    public static double[] createInterpTime(double[] time, double fs) {
        double step = 1.0 / fs;
        int n = (int) Math.floor(time[time.length - 1] / step + 1e-9) + 1;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            out[i] = i * step;
        }
        return out;
    }

    public static double[] interpCubicSpline(double[] rri, double[] time, double fs) {
        double[] xt = createInterpTime(time, fs);
        PolynomialSplineFunction f = new SplineInterpolator().interpolate(time, rri);
        double[] out = new double[xt.length];
        for (int i = 0; i < xt.length; i++) {
            out[i] = f.value(xt[i]);
        }
        return out;
    }

    public static double[] interpLinear(double[] rri, double[] time, double fs) {
        double[] xt = createInterpTime(time, fs);
        PolynomialSplineFunction f = new LinearInterpolator().interpolate(time, rri);
        double[] out = new double[xt.length];
        for (int i = 0; i < xt.length; i++) {
            out[i] = f.value(xt[i]);
        }
        return out;
    }

    public static double[] interpolateRri(double[] rri, double[] time, double fs, String method) {
        if ("linear".equalsIgnoreCase(method)) {
            return interpLinear(rri, time, fs);
        }
        return interpCubicSpline(rri, time, fs);
    }

    public static double median(double[] x) {
        double[] c = Arrays.copyOf(x, x.length);
        Arrays.sort(c);
        if (c.length % 2 == 1) {
            return c[c.length / 2];
        }
        return 0.5 * (c[c.length / 2 - 1] + c[c.length / 2]);
    }

    public static double mean(double[] x) {
        double s = 0.0;
        for (double v : x) s += v;
        return s / x.length;
    }

    public static double std(double[] x, boolean ddof1) {
        double mu = mean(x);
        double ss = 0.0;
        for (double v : x) {
            double d = v - mu;
            ss += d * d;
        }
        int den = ddof1 ? x.length - 1 : x.length;
        return Math.sqrt(ss / den);
    }

    public static double[] diff(double[] x) {
        double[] out = new double[x.length - 1];
        for (int i = 0; i < out.length; i++) {
            out[i] = x[i + 1] - x[i];
        }
        return out;
    }
}
