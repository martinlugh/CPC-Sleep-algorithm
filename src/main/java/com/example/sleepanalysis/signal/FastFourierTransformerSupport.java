package com.example.sleepanalysis.signal;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 * FFT 支撑类（基于 Apache Commons Math3）。
 */
public class FastFourierTransformerSupport {

    /** FFT 实例。 */
    private final FastFourierTransformer fft;

    /**
     * 构造函数。
     */
    public FastFourierTransformerSupport() {
        this.fft = new FastFourierTransformer(DftNormalization.STANDARD);
    }

    /**
     * 对输入信号执行实数 FFT，长度自动补齐到 2 的幂。
     *
     * @param realSignal 实数信号
     * @return 复数频谱
     */
    public Complex[] transformReal(double[] realSignal) {
        if (realSignal == null || realSignal.length == 0) {
            return new Complex[0];
        }

        int fftSize = nextPowerOfTwo(realSignal.length);
        double[] padded = new double[fftSize];
        System.arraycopy(realSignal, 0, padded, 0, realSignal.length);

        return fft.transform(padded, TransformType.FORWARD);
    }

    /**
     * 计算单边幅值谱（不含额外归一化因子）。
     *
     * @param spectrum 复数频谱
     * @return 单边幅值数组
     */
    public double[] oneSidedMagnitude(Complex[] spectrum) {
        if (spectrum == null || spectrum.length == 0) {
            return new double[0];
        }

        int half = spectrum.length / 2;
        double[] magnitude = new double[half + 1];

        for (int i = 0; i <= half; i++) {
            magnitude[i] = spectrum[i].abs();
        }

        return magnitude;
    }

    /**
     * 生成单边频率轴。
     *
     * @param fftSize FFT 点数
     * @param sampleRateHz 采样率
     * @return 单边频率数组
     */
    public double[] oneSidedFrequencyAxis(int fftSize, double sampleRateHz) {
        if (fftSize <= 0 || sampleRateHz <= 0.0D) {
            return new double[0];
        }

        int half = fftSize / 2;
        double[] frequencies = new double[half + 1];
        double bin = sampleRateHz / fftSize;
        for (int i = 0; i <= half; i++) {
            frequencies[i] = i * bin;
        }
        return frequencies;
    }

    /**
     * 计算大于等于 n 的最小 2 的幂。
     */
    public int nextPowerOfTwo(int n) {
        int value = 1;
        while (value < n) {
            value <<= 1;
        }
        return value;
    }
}
