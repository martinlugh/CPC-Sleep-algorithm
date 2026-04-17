package com.example.sleepanalysis.signal;

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * 信号重采样器（Akima 样条插值）。
 */
public class SignalResampler {

    /** 目标采样率（Hz）。 */
    public static final double TARGET_SAMPLE_RATE_HZ = 4.0D;

    /**
     * 将不等间隔 RRI（毫秒）重采样为 4Hz 等间隔序列（秒）。
     *
     * @param cleanedRriMillis 已清洗 RRI（毫秒）
     * @return 4Hz 重采样结果（秒）
     */
    public double[] resampleTo4HzSeconds(double[] cleanedRriMillis) {
        return resampleToTargetRateSeconds(cleanedRriMillis, TARGET_SAMPLE_RATE_HZ);
    }

    /**
     * 将不等间隔 RRI（毫秒）重采样为目标采样率等间隔序列（秒）。
     *
     * @param cleanedRriMillis 已清洗 RRI（毫秒）
     * @param targetSampleRateHz 目标采样率
     * @return 重采样结果（秒）
     */
    public double[] resampleToTargetRateSeconds(double[] cleanedRriMillis, double targetSampleRateHz) {
        if (cleanedRriMillis == null || cleanedRriMillis.length < 2 || targetSampleRateHz <= 0.0D) {
            return new double[0];
        }

        int n = cleanedRriMillis.length;
        double[] irregularTimeAxisSeconds = new double[n];
        double[] rriSeconds = new double[n];

        double cumulativeTimeSec = 0.0D;
        for (int i = 0; i < n; i++) {
            rriSeconds[i] = cleanedRriMillis[i] / 1000.0D;
            cumulativeTimeSec += rriSeconds[i];
            irregularTimeAxisSeconds[i] = cumulativeTimeSec;
        }

        double start = irregularTimeAxisSeconds[0];
        double end = irregularTimeAxisSeconds[n - 1];
        if (end <= start) {
            return new double[0];
        }

        double step = 1.0D / targetSampleRateHz;
        int outputSize = (int) Math.floor((end - start) / step) + 1;
        if (outputSize < 2) {
            return new double[0];
        }

        double[] regularTimeAxis = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            regularTimeAxis[i] = start + i * step;
        }

        AkimaSplineInterpolator akimaSplineInterpolator = new AkimaSplineInterpolator();
        PolynomialSplineFunction splineFunction = akimaSplineInterpolator.interpolate(irregularTimeAxisSeconds, rriSeconds);

        double[] resampled = new double[outputSize];
        double lastX = irregularTimeAxisSeconds[n - 1];
        for (int i = 0; i < outputSize; i++) {
            double x = regularTimeAxis[i];
            if (x > lastX) {
                x = lastX;
            }
            resampled[i] = splineFunction.value(x);
        }

        return resampled;
    }
}
