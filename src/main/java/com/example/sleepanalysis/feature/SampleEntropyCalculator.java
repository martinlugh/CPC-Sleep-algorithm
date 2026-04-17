package com.example.sleepanalysis.feature;

/**
 * 样本熵计算器（优化内存版本）。
 * <p>
 * 固定参数：m=2，r=0.2*std。
 * 复杂度 O(N²)，不分配二维矩阵。
 * </p>
 */
public class SampleEntropyCalculator {

    /** 模板维度 m。 */
    private static final int M = 2;

    /** 容忍度系数。 */
    private static final double R_FACTOR = 0.2D;

    /**
     * 计算样本熵。
     *
     * @param series 输入序列
     * @return SampEn
     */
    public double calculate(double[] series) {
        if (series == null || series.length <= (M + 1)) {
            return 0.0D;
        }

        int n = series.length;
        double std = standardDeviation(series);
        if (std <= 1.0E-12D) {
            return 0.0D;
        }
        double r = R_FACTOR * std;

        double bCount = 0.0D;
        double aCount = 0.0D;

        int upperM = n - M;
        int upperM1 = n - (M + 1);

        for (int i = 0; i < upperM; i++) {
            for (int j = i + 1; j < upperM; j++) {
                if (isMatch(series, i, j, M, r)) {
                    bCount += 1.0D;
                    if (i < upperM1 && j < upperM1 && isMatch(series, i, j, M + 1, r)) {
                        aCount += 1.0D;
                    }
                }
            }
        }

        if (bCount <= 0.0D || aCount <= 0.0D) {
            return 0.0D;
        }

        return -Math.log(aCount / bCount);
    }

    /**
     * 判断两个模板向量是否匹配（切比雪夫距离）。
     */
    private boolean isMatch(double[] series, int i, int j, int m, double r) {
        for (int k = 0; k < m; k++) {
            if (Math.abs(series[i + k] - series[j + k]) > r) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算样本标准差。
     */
    private double standardDeviation(double[] values) {
        int n = values.length;
        if (n < 2) {
            return 0.0D;
        }

        double mean = 0.0D;
        for (int i = 0; i < n; i++) {
            mean += values[i];
        }
        mean /= n;

        double sum = 0.0D;
        for (int i = 0; i < n; i++) {
            double d = values[i] - mean;
            sum += d * d;
        }

        return Math.sqrt(sum / (n - 1));
    }
}
