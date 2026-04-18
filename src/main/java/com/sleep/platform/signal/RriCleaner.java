package com.sleep.platform.signal;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class RriCleaner {

    public CleaningResult clean(List<Double> rriMsList) {
        if (rriMsList == null || rriMsList.isEmpty()) {
            return new CleaningResult(new double[0], 0, 0, 1.0);
        }
        List<Double> physiologicalRangeList = new ArrayList<>();
        for (Double value : rriMsList) {
            if (value != null && value >= 300.0 && value <= 1500.0) {
                physiologicalRangeList.add(value);
            }
        }
        if (physiologicalRangeList.isEmpty()) {
            return new CleaningResult(new double[0], rriMsList.size(), 0, 1.0);
        }

        List<Double> deSpikeList = removeLocalOutlier(physiologicalRangeList);
        int rawCount = rriMsList.size();
        int cleanedCount = deSpikeList.size();
        double abnormalRatio = rawCount == 0 ? 1.0 : (rawCount - cleanedCount) * 1.0 / rawCount;
        double[] cleanedArray = deSpikeList.stream().mapToDouble(Double::doubleValue).toArray();
        return new CleaningResult(cleanedArray, rawCount, cleanedCount, abnormalRatio);
    }

    private List<Double> removeLocalOutlier(List<Double> input) {
        if (input.size() <= 3) {
            return new ArrayList<>(input);
        }
        List<Double> output = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            double current = input.get(i);
            int from = Math.max(0, i - 2);
            int to = Math.min(input.size() - 1, i + 2);
            double[] neighborhood = new double[to - from + 1];
            for (int j = from; j <= to; j++) {
                neighborhood[j - from] = input.get(j);
            }
            Arrays.sort(neighborhood);
            double median = neighborhood[neighborhood.length / 2];
            double threshold = Math.max(80.0, median * 0.25);
            if (Math.abs(current - median) <= threshold) {
                output.add(current);
            }
        }
        return output;
    }

    public static class CleaningResult {
        private final double[] rriMsArray;
        private final int rawCount;
        private final int cleanedCount;
        private final double abnormalRatio;

        public CleaningResult(double[] rriMsArray, int rawCount, int cleanedCount, double abnormalRatio) {
            this.rriMsArray = rriMsArray;
            this.rawCount = rawCount;
            this.cleanedCount = cleanedCount;
            this.abnormalRatio = abnormalRatio;
        }

        public double[] getRriMsArray() {
            return rriMsArray;
        }

        public int getRawCount() {
            return rawCount;
        }

        public int getCleanedCount() {
            return cleanedCount;
        }

        public double getAbnormalRatio() {
            return abnormalRatio;
        }
    }
}
