package com.example.sleepanalysis.feature;

/**
 * HRV 统计特征计算器。
 */
public class HrvStatisticsCalculator {

    /**
     * 计算常见 HRV 时域统计量。
     *
     * @param nnIntervalsMs NN 间期（毫秒）
     * @return HRV 指标
     */
    public HrvStatistics analyze(double[] nnIntervalsMs) {
        if (nnIntervalsMs == null || nnIntervalsMs.length < 2) {
            return new HrvStatistics(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        int n = nnIntervalsMs.length;
        double mean = 0.0D;
        for (int i = 0; i < n; i++) {
            mean += nnIntervalsMs[i];
        }
        mean /= n;

        double sdnnVar = 0.0D;
        for (int i = 0; i < n; i++) {
            double d = nnIntervalsMs[i] - mean;
            sdnnVar += d * d;
        }
        sdnnVar /= (n - 1);
        double sdnn = Math.sqrt(Math.max(sdnnVar, 0.0D));

        int diffN = n - 1;
        double sumSq = 0.0D;
        int nn50Count = 0;
        for (int i = 0; i < diffN; i++) {
            double diff = nnIntervalsMs[i + 1] - nnIntervalsMs[i];
            sumSq += diff * diff;
            if (Math.abs(diff) > 50.0D) {
                nn50Count++;
            }
        }

        double rmssd = Math.sqrt(sumSq / diffN);
        double pnn50 = (double) nn50Count / diffN;

        // 心率（bpm）约等于 60000 / NN(ms)
        double meanHeartRate = mean <= 1.0E-12D ? 0.0D : 60000.0D / mean;

        return new HrvStatistics(mean, meanHeartRate, sdnn, rmssd, pnn50);
    }

    /**
     * HRV 指标封装。
     */
    public static class HrvStatistics {
        private final double meanNnMs;
        private final double meanHeartRateBpm;
        private final double sdnnMs;
        private final double rmssdMs;
        private final double pnn50;

        public HrvStatistics(double meanNnMs, double meanHeartRateBpm, double sdnnMs, double rmssdMs, double pnn50) {
            this.meanNnMs = meanNnMs;
            this.meanHeartRateBpm = meanHeartRateBpm;
            this.sdnnMs = sdnnMs;
            this.rmssdMs = rmssdMs;
            this.pnn50 = pnn50;
        }

        public double getMeanNnMs() {
            return meanNnMs;
        }

        public double getMeanHeartRateBpm() {
            return meanHeartRateBpm;
        }

        public double getSdnnMs() {
            return sdnnMs;
        }

        public double getRmssdMs() {
            return rmssdMs;
        }

        public double getPnn50() {
            return pnn50;
        }
    }
}
