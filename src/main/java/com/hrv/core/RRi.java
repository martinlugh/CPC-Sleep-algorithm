package com.hrv.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RR 间期序列对象，对应 Python 版 RRi。
 */
public class RRi {
    protected final double[] rri;
    protected double[] time;
    protected final boolean detrended;
    protected final boolean interpolated;

    public RRi(double[] rawRri) {
        this(rawRri, null, false, false);
    }

    public RRi(double[] rawRri, double[] time) {
        this(rawRri, time, false, false);
    }

    protected RRi(double[] rawRri, double[] inTime, boolean detrended, boolean interpolated) {
        this.rri = HrvUtils.transformRri(rawRri);
        this.detrended = detrended;
        this.interpolated = interpolated;
        if (inTime == null) {
            this.time = HrvUtils.createTimeInfo(this.rri);
        } else {
            validateTime(this.rri, inTime);
            this.time = Arrays.copyOf(inTime, inTime.length);
        }
    }

    public static void validateTime(double[] rri, double[] time) {
        if (rri.length != time.length) {
            throw new IllegalArgumentException("rri 和 time 长度必须一致");
        }
        for (int i = 1; i < time.length; i++) {
            if (time[i] == 0.0) throw new IllegalArgumentException("time 除首元素外不能为 0");
            if (time[i] <= time[i - 1]) throw new IllegalArgumentException("time 必须严格递增");
        }
        for (double t : time) {
            if (t < 0) throw new IllegalArgumentException("time 不能出现负值");
        }
    }

    public int length() { return rri.length; }

    public double[] values() { return Arrays.copyOf(rri, rri.length); }

    public double[] time() { return Arrays.copyOf(time, time.length); }

    public boolean isDetrended() { return detrended; }

    public boolean isInterpolated() { return interpolated; }

    public double[] toHr() {
        double[] out = new double[rri.length];
        for (int i = 0; i < rri.length; i++) {
            out[i] = 60.0 / (rri[i] / 1000.0);
        }
        return out;
    }

    public RRi timeRange(double start, double end) {
        List<Double> rv = new ArrayList<>();
        List<Double> tv = new ArrayList<>();
        for (int i = 0; i < rri.length; i++) {
            if (time[i] >= start && time[i] <= end) {
                rv.add(rri[i]);
                tv.add(time[i]);
            }
        }
        return new RRi(toArray(rv), toArray(tv));
    }

    public RRi resetTime() {
        double t0 = time[0];
        double[] nt = Arrays.copyOf(time, time.length);
        for (int i = 0; i < nt.length; i++) nt[i] -= t0;
        return new RRi(rri, nt);
    }

    public void resetTimeInplace() {
        double t0 = time[0];
        for (int i = 0; i < time.length; i++) time[i] -= t0;
    }

    public double mean() { return HrvUtils.mean(rri); }
    public double var() {
        double s = HrvUtils.std(rri, false);
        return s * s;
    }
    public double std() { return HrvUtils.std(rri, false); }
    public double median() { return HrvUtils.median(rri); }
    public double min() { return Arrays.stream(rri).min().orElse(Double.NaN); }
    public double max() { return Arrays.stream(rri).max().orElse(Double.NaN); }
    public double amplitude() { return max() - min(); }
    public double rms() {
        double ss = 0.0;
        for (double v : rri) ss += v * v;
        return Math.sqrt(ss / rri.length);
    }

    public List<RRi> timeSplit(double segSize, double overlap, boolean keepLast) {
        double duration = time[time.length - 1];
        if (overlap > segSize) throw new IllegalArgumentException("overlap 不能大于 segSize");
        if (segSize > duration) throw new IllegalArgumentException("segSize 不能大于总时长");

        double begin = 0.0;
        double end = segSize;
        double step = segSize - overlap;
        int n = (int) ((duration - segSize) / step) + 1;
        List<RRi> segments = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean includeEnd = i + 1 == n;
            List<Double> rv = new ArrayList<>();
            List<Double> tv = new ArrayList<>();
            for (int k = 0; k < rri.length; k++) {
                boolean in = time[k] >= begin && (includeEnd ? time[k] <= end : time[k] < end);
                if (in) {
                    rv.add(rri[k]);
                    tv.add(time[k]);
                }
            }
            segments.add(new RRi(toArray(rv), toArray(tv)));
            begin += step;
            end += step;
        }

        if (keepLast && segments.get(segments.size() - 1).time()[segments.get(segments.size() - 1).length() - 1] < duration) {
            List<Double> rv = new ArrayList<>();
            List<Double> tv = new ArrayList<>();
            for (int k = 0; k < rri.length; k++) {
                if (time[k] > begin) {
                    rv.add(rri[k]);
                    tv.add(time[k]);
                }
            }
            segments.add(new RRi(toArray(rv), toArray(tv)));
        }
        return segments;
    }

    private static double[] toArray(List<Double> list) {
        double[] out = new double[list.size()];
        for (int i = 0; i < list.size(); i++) out[i] = list.get(i);
        return out;
    }
}
