package com.sleep.platform.feature;

import org.springframework.stereotype.Component;

@Component
public class SampleEntropyCalculator {

    public double calculate(double[] series) {
        if (series == null || series.length < 20) {
            return 0.0;
        }
        int m = 2;
        double std = std(series);
        double r = 0.2 * std;
        if (r == 0.0) {
            return 0.0;
        }
        double a = countMatches(series, m + 1, r);
        double b = countMatches(series, m, r);
        if (a <= 0.0 || b <= 0.0) {
            return 0.0;
        }
        return -Math.log(a / b);
    }

    private double countMatches(double[] data, int m, double r) {
        int n = data.length;
        double count = 0.0;
        for (int i = 0; i < n - m; i++) {
            for (int j = i + 1; j < n - m; j++) {
                if (maxDistance(data, i, j, m) < r) {
                    count += 1.0;
                }
            }
        }
        return count;
    }

    private double maxDistance(double[] data, int i, int j, int m) {
        double max = 0.0;
        for (int k = 0; k < m; k++) {
            max = Math.max(max, Math.abs(data[i + k] - data[j + k]));
        }
        return max;
    }

    private double std(double[] data) {
        double mean = 0.0;
        for (double v : data) {
            mean += v;
        }
        mean /= data.length;
        double sum = 0.0;
        for (double v : data) {
            double d = v - mean;
            sum += d * d;
        }
        return Math.sqrt(sum / Math.max(1, data.length - 1));
    }
}
