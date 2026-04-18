package com.sleep.platform.signal;

import org.springframework.stereotype.Component;

@Component
public class PowerSpectrumAnalyzer {

    public SpectrumBandResult analyze(FastFourierTransformerSupport.FftResult fftResult, double sampleRateHz) {
        double[] real = fftResult.getReal();
        double[] imag = fftResult.getImag();
        if (real.length == 0) {
            return new SpectrumBandResult(0.0, 0.0, 0.0, 0.0, 0.0);
        }
        int n = real.length;
        int half = n / 2;
        double vlf = 0.0;
        double lf = 0.0;
        double hf = 0.0;
        double total = 0.0;

        for (int i = 1; i <= half; i++) {
            double freq = i * sampleRateHz / n;
            double power = (real[i] * real[i] + imag[i] * imag[i]) / n;
            total += power;
            if (freq >= 0.0033 && freq < 0.04) {
                vlf += power;
            } else if (freq >= 0.04 && freq < 0.15) {
                lf += power;
            } else if (freq >= 0.15 && freq <= 0.40) {
                hf += power;
            }
        }
        double ratio = lf == 0.0 ? 0.0 : hf / lf;
        return new SpectrumBandResult(vlf, lf, hf, total, ratio);
    }

    public static class SpectrumBandResult {
        private final double vlfcPower;
        private final double lfcPower;
        private final double hfcPower;
        private final double totalPower;
        private final double hfcLfcRatio;

        public SpectrumBandResult(double vlfcPower, double lfcPower, double hfcPower, double totalPower, double hfcLfcRatio) {
            this.vlfcPower = vlfcPower;
            this.lfcPower = lfcPower;
            this.hfcPower = hfcPower;
            this.totalPower = totalPower;
            this.hfcLfcRatio = hfcLfcRatio;
        }

        public double getVlfcPower() {
            return vlfcPower;
        }

        public double getLfcPower() {
            return lfcPower;
        }

        public double getHfcPower() {
            return hfcPower;
        }

        public double getTotalPower() {
            return totalPower;
        }

        public double getHfcLfcRatio() {
            return hfcLfcRatio;
        }
    }
}
