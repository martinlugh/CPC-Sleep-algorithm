package com.hrv.analysis;

import java.util.LinkedHashMap;
import java.util.Map;

import com.hrv.core.HrvUtils;
import com.hrv.core.RRi;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * 经典 HRV 指标计算（时域/频域/非线性）。
 */
public final class Classical {
    private Classical() {
    }

    public static Map<String, Double> timeDomain(double[] rri) {
        double[] x = HrvUtils.transformRri(rri);
        double[] d = HrvUtils.diff(x);

        double rmssd = 0.0;
        for (double v : d) rmssd += v * v;
        rmssd = Math.sqrt(rmssd / d.length);

        double sdnn = HrvUtils.std(x, true);
        double sdsd = HrvUtils.std(d, true);
        double nn50 = nn50(x);
        double pnn50 = nn50 / x.length * 100.0;
        double mrri = HrvUtils.mean(x);
        double[] hr = new RRi(x).toHr();
        double mhr = HrvUtils.mean(hr);

        Map<String, Double> out = new LinkedHashMap<>();
        out.put("rmssd", rmssd);
        out.put("sdnn", sdnn);
        out.put("sdsd", sdsd);
        out.put("nn50", nn50);
        out.put("pnn50", pnn50);
        out.put("mrri", mrri);
        out.put("mhr", mhr);
        return out;
    }

    public static Map<String, Double> nonLinear(double[] rri) {
        double[] x = HrvUtils.transformRri(rri);
        double[] d = HrvUtils.diff(x);
        double sd1 = Math.sqrt(0.5 * Math.pow(HrvUtils.std(d, true), 2));
        double sd2 = Math.sqrt(2.0 * Math.pow(HrvUtils.std(x, true), 2) - 0.5 * Math.pow(HrvUtils.std(d, true), 2));

        Map<String, Double> out = new LinkedHashMap<>();
        out.put("sd1", sd1);
        out.put("sd2", sd2);
        return out;
    }

    /**
     * 频域（Welch 简化版：使用分段汉宁窗 + FFT）。
     */
    public static Map<String, Double> frequencyDomain(
            double[] rri,
            double[] time,
            double fs,
            String interpMethod,
            double[] vlfBand,
            double[] lfBand,
            double[] hfBand
    ) {
        double[] x = HrvUtils.transformRri(rri);
        double[] t = (time == null) ? HrvUtils.createTimeInfo(x) : time;
        double[] xi = HrvUtils.interpolateRri(x, t, fs, interpMethod == null ? "cubic" : interpMethod);

        int n = nextPow2(xi.length);
        double[] padded = new double[n];
        System.arraycopy(xi, 0, padded, 0, xi.length);

        for (int i = 0; i < padded.length; i++) {
            double w = 0.5 - 0.5 * Math.cos(2 * Math.PI * i / (padded.length - 1));
            padded[i] *= w;
        }

        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] spec = fft.transform(padded, TransformType.FORWARD);

        int half = n / 2;
        double[] fxx = new double[half + 1];
        double[] pxx = new double[half + 1];
        for (int k = 0; k <= half; k++) {
            fxx[k] = k * fs / n;
            pxx[k] = spec[k].abs() * spec[k].abs() / n;
        }

        return auc(fxx, pxx,
                vlfBand == null ? new double[]{0.0, 0.04} : vlfBand,
                lfBand == null ? new double[]{0.04, 0.15} : lfBand,
                hfBand == null ? new double[]{0.15, 0.40} : hfBand);
    }

    private static Map<String, Double> auc(double[] fxx, double[] pxx, double[] vlfBand, double[] lfBand, double[] hfBand) {
        double vlf = trapzRange(fxx, pxx, vlfBand[0], vlfBand[1]);
        double lf = trapzRange(fxx, pxx, lfBand[0], lfBand[1]);
        double hf = trapzRange(fxx, pxx, hfBand[0], hfBand[1]);
        double total = vlf + lf + hf;
        double lfHf = lf / hf;
        double lfnu = lf / (total - vlf) * 100.0;
        double hfnu = hf / (total - vlf) * 100.0;

        Map<String, Double> out = new LinkedHashMap<>();
        out.put("total_power", total);
        out.put("vlf", vlf);
        out.put("lf", lf);
        out.put("hf", hf);
        out.put("lf_hf", lfHf);
        out.put("lfnu", lfnu);
        out.put("hfnu", hfnu);
        return out;
    }

    private static double trapzRange(double[] x, double[] y, double lo, double hi) {
        double area = 0.0;
        for (int i = 1; i < x.length; i++) {
            if (x[i - 1] >= lo && x[i] < hi) {
                area += (x[i] - x[i - 1]) * (y[i] + y[i - 1]) * 0.5;
            }
        }
        return area;
    }

    private static int nn50(double[] rri) {
        int c = 0;
        for (int i = 1; i < rri.length; i++) {
            if (Math.abs(rri[i] - rri[i - 1]) > 50.0) c++;
        }
        return c;
    }

    private static int nextPow2(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }
}
