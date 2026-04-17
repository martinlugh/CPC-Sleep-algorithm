package com.example.sleepanalysis.feature;

/**
 * Poincaré 图特征分析（SD1/SD2）。
 */
public class PoincareAnalysis {

    /**
     * 计算 SD1、SD2、SDNN、SDSD。
     *
     * @param nnIntervalsMs NN 间期（毫秒）
     * @return Poincaré 指标
     */
    public PoincareMetrics analyze(double[] nnIntervalsMs) {
        if (nnIntervalsMs == null || nnIntervalsMs.length < 3) {
            return new PoincareMetrics(0.0D, 0.0D, 0.0D, 0.0D);
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
        double diffMean = 0.0D;
        double[] diffs = new double[diffN];
        for (int i = 0; i < diffN; i++) {
            double v = nnIntervalsMs[i + 1] - nnIntervalsMs[i];
            diffs[i] = v;
            diffMean += v;
        }
        diffMean /= diffN;

        double sdsdVar = 0.0D;
        for (int i = 0; i < diffN; i++) {
            double d = diffs[i] - diffMean;
            sdsdVar += d * d;
        }
        sdsdVar /= (diffN - 1);
        double sdsd = Math.sqrt(Math.max(sdsdVar, 0.0D));

        // SD1 = sqrt(0.5) * SDSD
        double sd1 = Math.sqrt(0.5D) * sdsd;

        // SD2 = sqrt(2 * SDNN² - 0.5 * SDSD²)
        double sd2Term = 2.0D * sdnn * sdnn - 0.5D * sdsd * sdsd;
        double sd2 = Math.sqrt(Math.max(sd2Term, 0.0D));

        return new PoincareMetrics(sd1, sd2, sdnn, sdsd);
    }

    /**
     * Poincaré 指标封装。
     */
    public static class PoincareMetrics {
        private final double sd1;
        private final double sd2;
        private final double sdnn;
        private final double sdsd;

        public PoincareMetrics(double sd1, double sd2, double sdnn, double sdsd) {
            this.sd1 = sd1;
            this.sd2 = sd2;
            this.sdnn = sdnn;
            this.sdsd = sdsd;
        }

        public double getSd1() {
            return sd1;
        }

        public double getSd2() {
            return sd2;
        }

        public double getSdnn() {
            return sdnn;
        }

        public double getSdsd() {
            return sdsd;
        }

        public double getSd2Sd1Ratio() {
            if (sd1 <= 1.0E-12D) {
                return 0.0D;
            }
            return sd2 / sd1;
        }
    }
}
