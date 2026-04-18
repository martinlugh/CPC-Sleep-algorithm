package com.sleep.platform.feature;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class PoincareAnalysis {

    public PoincareResult analyze(double[] rriMsArray) {
        if (rriMsArray == null || rriMsArray.length < 2) {
            return new PoincareResult(0.0, 0.0, 0.0);
        }
        int n = rriMsArray.length - 1;
        double[] diff = new double[n];
        double[] sum = new double[n];
        for (int i = 0; i < n; i++) {
            diff[i] = (rriMsArray[i + 1] - rriMsArray[i]) / Math.sqrt(2.0);
            sum[i] = (rriMsArray[i + 1] + rriMsArray[i]) / Math.sqrt(2.0);
        }
        double sd1 = std(diff);
        double sd2 = std(sum);
        double ratio = sd1 == 0.0 ? 0.0 : sd2 / sd1;
        return new PoincareResult(sd1, sd2, ratio);
    }

    private double std(double[] arr) {
        if (arr.length < 2) {
            return 0.0;
        }
        double mean = 0.0;
        for (double v : arr) {
            mean += v;
        }
        mean /= arr.length;
        double sum = 0.0;
        for (double v : arr) {
            double d = v - mean;
            sum += d * d;
        }
        return Math.sqrt(sum / (arr.length - 1));
    }

    @Data
    public static class PoincareResult {
        private final double sd1Ms;
        private final double sd2Ms;
        private final double sd2Sd1Ratio;
    }
}
