package com.sleep.platform.engine;

import com.sleep.platform.domain.model.CpcAnalysisResult;
import com.sleep.platform.signal.FastFourierTransformerSupport;
import com.sleep.platform.signal.PowerSpectrumAnalyzer;
import com.sleep.platform.signal.WindowFunctionApplier;
import org.springframework.stereotype.Component;

@Component
public class CpcAlgorithmEngine {

    private static final double SAMPLING_RATE_HZ = 4.0;

    private final WindowFunctionApplier windowFunctionApplier;
    private final FastFourierTransformerSupport fastFourierTransformerSupport;
    private final PowerSpectrumAnalyzer powerSpectrumAnalyzer;

    public CpcAlgorithmEngine(WindowFunctionApplier windowFunctionApplier,
                              FastFourierTransformerSupport fastFourierTransformerSupport,
                              PowerSpectrumAnalyzer powerSpectrumAnalyzer) {
        this.windowFunctionApplier = windowFunctionApplier;
        this.fastFourierTransformerSupport = fastFourierTransformerSupport;
        this.powerSpectrumAnalyzer = powerSpectrumAnalyzer;
    }

    public CpcAnalysisResult analyze(double[] resampledRriSeries, double[] derivedRespirationSeries) {
        double[] cpcSignal = buildCouplingSignal(resampledRriSeries, derivedRespirationSeries);
        double[] windowed = windowFunctionApplier.applyHannWindow(cpcSignal);
        FastFourierTransformerSupport.FftResult fftResult = fastFourierTransformerSupport.fft(windowed);
        PowerSpectrumAnalyzer.SpectrumBandResult spectrumBandResult = powerSpectrumAnalyzer.analyze(fftResult, SAMPLING_RATE_HZ);

        CpcAnalysisResult result = new CpcAnalysisResult();
        result.setHfcPower(spectrumBandResult.getHfcPower());
        result.setLfcPower(spectrumBandResult.getLfcPower());
        result.setVlfcPower(spectrumBandResult.getVlfcPower());
        result.setTotalPower(spectrumBandResult.getTotalPower());
        result.setHfcLfcRatio(spectrumBandResult.getHfcLfcRatio());

        double couplingScore = spectrumBandResult.getTotalPower() <= 0.0 ? 0.0
                : (spectrumBandResult.getHfcPower() - spectrumBandResult.getLfcPower()) / spectrumBandResult.getTotalPower();
        result.setCpcCouplingScore(couplingScore);
        if (couplingScore >= 0.15) {
            result.setCpcState("STABLE_COUPLING");
        } else if (couplingScore <= -0.15) {
            result.setCpcState("UNSTABLE_COUPLING");
        } else {
            result.setCpcState("TRANSITIONAL_COUPLING");
        }
        return result;
    }

    private double[] buildCouplingSignal(double[] resampledRriSeries, double[] derivedRespirationSeries) {
        int n = Math.min(resampledRriSeries.length, derivedRespirationSeries.length);
        if (n == 0) {
            return new double[0];
        }
        double[] output = new double[n];
        double meanRri = mean(resampledRriSeries, n);
        double meanResp = mean(derivedRespirationSeries, n);
        for (int i = 0; i < n; i++) {
            output[i] = (resampledRriSeries[i] - meanRri) * (derivedRespirationSeries[i] - meanResp);
        }
        return output;
    }

    private double mean(double[] arr, int n) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += arr[i];
        }
        return n == 0 ? 0.0 : sum / n;
    }
}
