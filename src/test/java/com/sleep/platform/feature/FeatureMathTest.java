package com.sleep.platform.feature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeatureMathTest {

    @Test
    void shouldCalculateSampleEntropy() {
        SampleEntropyCalculator calculator = new SampleEntropyCalculator();
        double[] data = new double[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = 800 + Math.sin(i * 0.15) * 20;
        }
        double entropy = calculator.calculate(data);
        Assertions.assertTrue(entropy >= 0.0);
    }

    @Test
    void shouldCalculateSd1Sd2() {
        PoincareAnalysis analysis = new PoincareAnalysis();
        double[] rri = {800, 810, 790, 805, 795, 802, 798};
        PoincareAnalysis.PoincareResult result = analysis.analyze(rri);
        Assertions.assertTrue(result.getSd1Ms() >= 0.0);
        Assertions.assertTrue(result.getSd2Ms() >= 0.0);
    }
}
