package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.domain.request.UserSleepBaselineProfile;
import com.example.sleepanalysis.enums.SleepStage;
import com.example.sleepanalysis.feature.CircadianPriorAdjuster;
import com.example.sleepanalysis.feature.HrvStatisticsCalculator;
import com.example.sleepanalysis.feature.PoincareAnalysis;

import java.time.OffsetDateTime;

/**
 * 睡眠初始分期决策引擎。
 */
public class SleepDecisionEngine {

    /** 昼夜先验调整器。 */
    private final CircadianPriorAdjuster circadianPriorAdjuster;

    /**
     * 构造函数。
     */
    public SleepDecisionEngine() {
        this.circadianPriorAdjuster = new CircadianPriorAdjuster();
    }

    /**
     * 依据特征生成初始阶段（WAKE/LIGHT/DEEP/REM）。
     *
     * @param cpcMetrics CPC 指标
     * @param poincareMetrics Poincaré 指标
     * @param hrvStatistics HRV 统计
     * @param sampleEntropy 样本熵
     * @param baselineProfile 用户基线
     * @param timestamp 时间戳
     * @return 初始决策结果
     */
    public SleepDecision decideInitialStage(
            CpcAlgorithmEngine.CpcMetrics cpcMetrics,
            PoincareAnalysis.PoincareMetrics poincareMetrics,
            HrvStatisticsCalculator.HrvStatistics hrvStatistics,
            double sampleEntropy,
            UserSleepBaselineProfile baselineProfile,
            OffsetDateTime timestamp) {

        NormalizedContext normalizedContext = normalizeByBaseline(hrvStatistics, poincareMetrics, baselineProfile);

        double wakeScore = 0.20D;
        double lightScore = 0.30D;
        double deepScore = 0.25D;
        double remScore = 0.25D;

        // 基于生理节律和频域耦合的规则性打分。
        if (normalizedContext.heartRateRatio > 1.10D || cpcMetrics.getVlfcPower() > cpcMetrics.getHfcPower()) {
            wakeScore += 0.30D;
            lightScore += 0.10D;
        }

        if (cpcMetrics.getHfcLfcRatio() > 1.15D && sampleEntropy < 1.20D) {
            deepScore += 0.30D;
            lightScore += 0.10D;
        }

        if (cpcMetrics.getLfcPower() > cpcMetrics.getHfcPower() && poincareMetrics.getSd2Sd1Ratio() > 2.0D) {
            remScore += 0.25D;
            lightScore += 0.10D;
        }

        if (sampleEntropy > 1.60D) {
            wakeScore += 0.20D;
            deepScore -= 0.10D;
            remScore -= 0.05D;
        }

        double[] adjusted = circadianPriorAdjuster.adjust(
                new double[]{wakeScore, lightScore, deepScore, remScore},
                timestamp,
                baselineProfile);

        int maxIndex = 0;
        double maxScore = adjusted[0];
        for (int i = 1; i < adjusted.length; i++) {
            if (adjusted[i] > maxScore) {
                maxScore = adjusted[i];
                maxIndex = i;
            }
        }

        SleepStage stage = mapIndexToStage(maxIndex);
        return new SleepDecision(stage, maxScore, adjusted[0], adjusted[1], adjusted[2], adjusted[3]);
    }

    /**
     * 使用用户基线对关键特征做归一化。
     */
    private NormalizedContext normalizeByBaseline(
            HrvStatisticsCalculator.HrvStatistics hrvStatistics,
            PoincareAnalysis.PoincareMetrics poincareMetrics,
            UserSleepBaselineProfile baselineProfile) {

        double baselineHeartRate = 60.0D;
        double baselineRespRate = 15.0D;

        if (baselineProfile != null) {
            if (baselineProfile.getBaselineRestingHeartRate() != null && baselineProfile.getBaselineRestingHeartRate() > 1.0D) {
                baselineHeartRate = baselineProfile.getBaselineRestingHeartRate();
            }
            if (baselineProfile.getBaselineRespirationRate() != null && baselineProfile.getBaselineRespirationRate() > 1.0D) {
                baselineRespRate = baselineProfile.getBaselineRespirationRate();
            }
        }

        double hrRatio = hrvStatistics.getMeanHeartRateBpm() <= 1.0E-12D
                ? 1.0D
                : hrvStatistics.getMeanHeartRateBpm() / baselineHeartRate;

        double variabilityRatio = poincareMetrics.getSd1() <= 1.0E-12D
                ? 1.0D
                : poincareMetrics.getSd2() / Math.max(poincareMetrics.getSd1(), 1.0E-12D);

        return new NormalizedContext(hrRatio, baselineRespRate, variabilityRatio);
    }

    /**
     * 索引映射到睡眠阶段。
     */
    private SleepStage mapIndexToStage(int index) {
        if (index == 0) {
            return SleepStage.WAKE;
        }
        if (index == 1) {
            return SleepStage.LIGHT;
        }
        if (index == 2) {
            return SleepStage.DEEP;
        }
        if (index == 3) {
            return SleepStage.REM;
        }
        return SleepStage.UNKNOWN;
    }

    /**
     * 归一化上下文。
     */
    private static class NormalizedContext {
        private final double heartRateRatio;
        private final double baselineRespRate;
        private final double variabilityRatio;

        private NormalizedContext(double heartRateRatio, double baselineRespRate, double variabilityRatio) {
            this.heartRateRatio = heartRateRatio;
            this.baselineRespRate = baselineRespRate;
            this.variabilityRatio = variabilityRatio;
        }
    }

    /**
     * 睡眠决策结果。
     */
    public static class SleepDecision {
        private final SleepStage stage;
        private final double confidence;
        private final double wakeScore;
        private final double lightScore;
        private final double deepScore;
        private final double remScore;

        public SleepDecision(SleepStage stage, double confidence, double wakeScore, double lightScore, double deepScore, double remScore) {
            this.stage = stage;
            this.confidence = confidence;
            this.wakeScore = wakeScore;
            this.lightScore = lightScore;
            this.deepScore = deepScore;
            this.remScore = remScore;
        }

        public SleepStage getStage() {
            return stage;
        }

        public double getConfidence() {
            return confidence;
        }

        public double getWakeScore() {
            return wakeScore;
        }

        public double getLightScore() {
            return lightScore;
        }

        public double getDeepScore() {
            return deepScore;
        }

        public double getRemScore() {
            return remScore;
        }
    }
}
