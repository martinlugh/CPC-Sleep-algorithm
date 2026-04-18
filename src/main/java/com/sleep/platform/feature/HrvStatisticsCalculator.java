package com.sleep.platform.feature;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class HrvStatisticsCalculator {

    public HrvStatistics calculate(double[] rriMsArray) {
        if (rriMsArray == null || rriMsArray.length == 0) {
            return new HrvStatistics(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }
        double meanNn = mean(rriMsArray);
        double sdnn = std(rriMsArray, meanNn);
        double rmssd = rmssd(rriMsArray);
        double pnn50 = pnn50(rriMsArray);
        double meanHr = meanNn == 0.0 ? 0.0 : 60000.0 / meanNn;
        double maxMinDiff = max(rriMsArray) - min(rriMsArray);
        return new HrvStatistics(meanNn, sdnn, rmssd, pnn50, meanHr, maxMinDiff);
    }

    private double mean(double[] arr) {
        double sum = 0.0;
        for (double v : arr) {
            sum += v;
        }
        return sum / arr.length;
    }

    private double std(double[] arr, double mean) {
        if (arr.length < 2) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : arr) {
            double d = v - mean;
            sum += d * d;
        }
        return Math.sqrt(sum / (arr.length - 1));
    }

    private double rmssd(double[] arr) {
        if (arr.length < 2) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = 1; i < arr.length; i++) {
            double diff = arr[i] - arr[i - 1];
            sum += diff * diff;
        }
        return Math.sqrt(sum / (arr.length - 1));
    }

    private double pnn50(double[] arr) {
        if (arr.length < 2) {
            return 0.0;
        }
        int count = 0;
        for (int i = 1; i < arr.length; i++) {
            if (Math.abs(arr[i] - arr[i - 1]) > 50.0) {
                count++;
            }
        }
        return count * 1.0 / (arr.length - 1);
    }

    private double max(double[] arr) {
        double max = Double.NEGATIVE_INFINITY;
        for (double v : arr) {
            max = Math.max(max, v);
        }
        return max;
    }

    private double min(double[] arr) {
        double min = Double.POSITIVE_INFINITY;
        for (double v : arr) {
            min = Math.min(min, v);
        }
        return min;
    }

    @Data
    public static class HrvStatistics {
        private final double meanNnMs;
        private final double sdnnMs;
        private final double rmssdMs;
        private final double pnn50;
        private final double meanHeartRateBpm;
        private final double rriRangeMs;
    }
}
