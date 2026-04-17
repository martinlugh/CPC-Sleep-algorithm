package com.example.sleepanalysis.signal;

import org.apache.commons.math3.complex.Complex;

/**
 * 功率谱密度分析器。
 */
public class PowerSpectrumAnalyzer {

    /** 窗函数应用器。 */
    private final WindowFunctionApplier windowFunctionApplier;

    /** FFT 支撑类。 */
    private final FastFourierTransformerSupport fftSupport;

    /**
     * 构造函数。
     */
    public PowerSpectrumAnalyzer() {
        this.windowFunctionApplier = new WindowFunctionApplier();
        this.fftSupport = new FastFourierTransformerSupport();
    }

    /**
     * 计算单边功率谱密度（PSD）。
     *
     * @param signal 输入信号
     * @param sampleRateHz 采样率（Hz）
     * @return PSD 结果（频率轴 + 功率谱）
     */
    public PsdResult analyze(double[] signal, double sampleRateHz) {
        if (signal == null || signal.length == 0 || sampleRateHz <= 0.0D) {
            return new PsdResult(new double[0], new double[0]);
        }

        int originalLength = signal.length;
        int fftSize = fftSupport.nextPowerOfTwo(originalLength);

        double[] windowed = new double[fftSize];
        System.arraycopy(signal, 0, windowed, 0, originalLength);
        windowFunctionApplier.applyHanningInPlace(windowed);

        Complex[] spectrum = fftSupport.transformReal(windowed);
        int half = spectrum.length / 2;

        double[] frequencies = fftSupport.oneSidedFrequencyAxis(spectrum.length, sampleRateHz);
        double[] powerSpectrum = new double[half + 1];

        double scale = 1.0D / (sampleRateHz * fftSize);
        for (int i = 0; i <= half; i++) {
            double re = spectrum[i].getReal();
            double im = spectrum[i].getImaginary();
            double power = (re * re + im * im) * scale;

            if (i > 0 && i < half) {
                power *= 2.0D;
            }

            powerSpectrum[i] = power;
        }

        return new PsdResult(frequencies, powerSpectrum);
    }

    /**
     * 对指定频段执行功率积分（梯形积分）。
     *
     * @param frequencies 频率轴
     * @param powerSpectrum 功率谱
     * @param lowHz 下限频率
     * @param highHz 上限频率
     * @return 频段功率
     */
    public double integrateBandPower(double[] frequencies, double[] powerSpectrum, double lowHz, double highHz) {
        if (frequencies == null || powerSpectrum == null || frequencies.length == 0 || powerSpectrum.length == 0) {
            return 0.0D;
        }
        if (frequencies.length != powerSpectrum.length || highHz <= lowHz) {
            return 0.0D;
        }

        double total = 0.0D;
        for (int i = 1; i < frequencies.length; i++) {
            double f0 = frequencies[i - 1];
            double f1 = frequencies[i];

            if (f1 < lowHz || f0 > highHz) {
                continue;
            }

            double left = f0 < lowHz ? lowHz : f0;
            double right = f1 > highHz ? highHz : f1;
            if (right <= left) {
                continue;
            }

            double p0 = powerSpectrum[i - 1];
            double p1 = powerSpectrum[i];

            // 对被频段截断的边界进行线性插值修正。
            double span = f1 - f0;
            if (span <= 0.0D) {
                continue;
            }

            double ratioLeft = (left - f0) / span;
            double ratioRight = (right - f0) / span;
            double leftPower = p0 + (p1 - p0) * ratioLeft;
            double rightPower = p0 + (p1 - p0) * ratioRight;

            total += 0.5D * (leftPower + rightPower) * (right - left);
        }

        return total;
    }

    /**
     * PSD 结果对象。
     */
    public static class PsdResult {

        /** 频率轴。 */
        private final double[] frequencies;

        /** 单边功率谱。 */
        private final double[] powerSpectrum;

        public PsdResult(double[] frequencies, double[] powerSpectrum) {
            this.frequencies = frequencies;
            this.powerSpectrum = powerSpectrum;
        }

        public double[] getFrequencies() {
            return frequencies;
        }

        public double[] getPowerSpectrum() {
            return powerSpectrum;
        }
    }
}
