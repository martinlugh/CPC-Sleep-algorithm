package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.signal.PowerSpectrumAnalyzer;

/**
 * CPC（Cardiopulmonary Coupling）算法引擎。
 */
public class CpcAlgorithmEngine {

    /** 高频耦合频段下限。 */
    private static final double HFC_LOW_HZ = 0.15D;

    /** 高频耦合频段上限。 */
    private static final double HFC_HIGH_HZ = 0.40D;

    /** 低频耦合频段下限。 */
    private static final double LFC_LOW_HZ = 0.01D;

    /** 低频耦合频段上限。 */
    private static final double LFC_HIGH_HZ = 0.10D;

    /** 极低频耦合频段上限。 */
    private static final double VLFC_HIGH_HZ = 0.01D;

    /** 功率谱分析器。 */
    private final PowerSpectrumAnalyzer powerSpectrumAnalyzer;

    /**
     * 构造函数。
     */
    public CpcAlgorithmEngine() {
        this.powerSpectrumAnalyzer = new PowerSpectrumAnalyzer();
    }

    /**
     * 计算 CPC 关键频域指标。
     *
     * @param respiratorySignal 呼吸相关信号
     * @param sampleRateHz 采样率
     * @return CPC 指标
     */
    public CpcMetrics analyze(double[] respiratorySignal, double sampleRateHz) {
        PowerSpectrumAnalyzer.PsdResult psdResult = powerSpectrumAnalyzer.analyze(respiratorySignal, sampleRateHz);
        double[] frequencies = psdResult.getFrequencies();
        double[] spectrum = psdResult.getPowerSpectrum();

        double hfcPower = powerSpectrumAnalyzer.integrateBandPower(frequencies, spectrum, HFC_LOW_HZ, HFC_HIGH_HZ);
        double lfcPower = powerSpectrumAnalyzer.integrateBandPower(frequencies, spectrum, LFC_LOW_HZ, LFC_HIGH_HZ);
        double vlfcPower = powerSpectrumAnalyzer.integrateBandPower(frequencies, spectrum, 0.0D, VLFC_HIGH_HZ);

        double ratio = lfcPower <= 1.0E-12D ? 0.0D : hfcPower / lfcPower;

        return new CpcMetrics(hfcPower, lfcPower, vlfcPower, ratio);
    }

    /**
     * CPC 指标封装。
     */
    public static class CpcMetrics {
        private final double hfcPower;
        private final double lfcPower;
        private final double vlfcPower;
        private final double hfcLfcRatio;

        public CpcMetrics(double hfcPower, double lfcPower, double vlfcPower, double hfcLfcRatio) {
            this.hfcPower = hfcPower;
            this.lfcPower = lfcPower;
            this.vlfcPower = vlfcPower;
            this.hfcLfcRatio = hfcLfcRatio;
        }

        public double getHfcPower() {
            return hfcPower;
        }

        public double getLfcPower() {
            return lfcPower;
        }

        public double getVlfcPower() {
            return vlfcPower;
        }

        public double getHfcLfcRatio() {
            return hfcLfcRatio;
        }
    }
}
