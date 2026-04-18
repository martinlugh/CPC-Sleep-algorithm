package com.sleep.platform.signal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PowerSpectrumAnalyzerTest {

    @Test
    void shouldComputeHfcLfcRatioAsHfDividedByLf() {
        int n = 256;
        double[] real = new double[n];
        double[] imag = new double[n];
        // 采样率4Hz时，bin频率=4/256=0.015625Hz
        // LF频段示例bin: 6(0.09375Hz)，HF频段示例bin: 14(0.21875Hz)
        real[6] = 4.0;
        real[14] = 10.0;

        FastFourierTransformerSupport.FftResult fftResult = new FastFourierTransformerSupport.FftResult(real, imag);
        PowerSpectrumAnalyzer analyzer = new PowerSpectrumAnalyzer();
        PowerSpectrumAnalyzer.SpectrumBandResult result = analyzer.analyze(fftResult, 4.0);

        Assertions.assertTrue(result.getHfcPower() > result.getLfcPower());
        Assertions.assertTrue(result.getHfcLfcRatio() > 1.0);
    }
}
