package com.example.sleepanalysis.signal;

/**
 * 呼吸信号提取器。
 * <p>
 * 基于 RSA（呼吸性窦性心律不齐）近似思路，
 * 对重采样后的 RRI 序列执行 0.15~0.45Hz 带通滤波。
 * </p>
 */
public class RespiratoryExtractor {

    /** 呼吸带通下限（Hz）。 */
    private static final double LOW_CUT_HZ = 0.15D;

    /** 呼吸带通上限（Hz）。 */
    private static final double HIGH_CUT_HZ = 0.45D;

    /**
     * 提取呼吸相关信号。
     *
     * @param rriResampledSeconds 4Hz 重采样后的 RRI（秒）
     * @param sampleRateHz 采样率（Hz）
     * @return 呼吸相关信号
     */
    public double[] extractRespiratorySignal(double[] rriResampledSeconds, double sampleRateHz) {
        if (rriResampledSeconds == null || rriResampledSeconds.length == 0 || sampleRateHz <= 0.0D) {
            return new double[0];
        }

        int n = rriResampledSeconds.length;
        double[] centered = new double[n];

        double mean = 0.0D;
        for (int i = 0; i < n; i++) {
            mean += rriResampledSeconds[i];
        }
        mean /= n;

        for (int i = 0; i < n; i++) {
            centered[i] = rriResampledSeconds[i] - mean;
        }

        // 采用高通 + 低通串联近似带通。
        double[] highPassed = firstOrderHighPass(centered, sampleRateHz, LOW_CUT_HZ);
        return firstOrderLowPass(highPassed, sampleRateHz, HIGH_CUT_HZ);
    }

    /**
     * 一阶高通滤波器（离散 RC 形式）。
     */
    private double[] firstOrderHighPass(double[] input, double fs, double cutoffHz) {
        int n = input.length;
        double[] output = new double[n];

        double dt = 1.0D / fs;
        double rc = 1.0D / (2.0D * Math.PI * cutoffHz);
        double alpha = rc / (rc + dt);

        double prevY = 0.0D;
        double prevX = input[0];
        output[0] = 0.0D;

        for (int i = 1; i < n; i++) {
            double x = input[i];
            double y = alpha * (prevY + x - prevX);
            output[i] = y;
            prevY = y;
            prevX = x;
        }

        return output;
    }

    /**
     * 一阶低通滤波器（离散 RC 形式）。
     */
    private double[] firstOrderLowPass(double[] input, double fs, double cutoffHz) {
        int n = input.length;
        double[] output = new double[n];

        double dt = 1.0D / fs;
        double rc = 1.0D / (2.0D * Math.PI * cutoffHz);
        double alpha = dt / (rc + dt);

        double prevY = input[0];
        output[0] = prevY;

        for (int i = 1; i < n; i++) {
            double y = prevY + alpha * (input[i] - prevY);
            output[i] = y;
            prevY = y;
        }

        return output;
    }
}
