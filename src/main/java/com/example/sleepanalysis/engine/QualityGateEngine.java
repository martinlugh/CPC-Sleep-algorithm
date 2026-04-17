package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.domain.request.SleepSegmentInput;
import com.example.sleepanalysis.enums.SleepStage;

import java.util.List;

/**
 * 数据质量门控引擎。
 */
public class QualityGateEngine {

    /** 丢失率阈值。 */
    private static final double MISSING_RATE_THRESHOLD = 0.20D;

    /**
     * 评估片段数据质量。
     *
     * @param segmentInput 片段输入
     * @return 质量门控结果
     */
    public QualityGateResult evaluate(SleepSegmentInput segmentInput) {
        if (segmentInput == null) {
            return new QualityGateResult(1.0D, false, SleepStage.UNKNOWN, "片段为空，无法分析");
        }

        MissingStats heartStats = missingStats(segmentInput.getHeartRateSeries());
        MissingStats respirationStats = missingStats(segmentInput.getRespirationRateSeries());
        MissingStats movementStats = missingStats(segmentInput.getBodyMovementSeries());

        int total = heartStats.total + respirationStats.total + movementStats.total;
        int missing = heartStats.missing + respirationStats.missing + movementStats.missing;

        if (total == 0) {
            return new QualityGateResult(1.0D, false, SleepStage.UNKNOWN, "核心信号为空");
        }

        double missingRate = (double) missing / total;
        if (missingRate > MISSING_RATE_THRESHOLD) {
            return new QualityGateResult(missingRate, false, SleepStage.UNKNOWN, "丢失率超过20%，强制UNKNOWN");
        }

        return new QualityGateResult(missingRate, true, null, "质量通过");
    }

    /**
     * 统计序列缺失情况。
     */
    private MissingStats missingStats(List<Double> series) {
        if (series == null || series.isEmpty()) {
            return new MissingStats(0, 0);
        }

        int missing = 0;
        int total = series.size();
        for (int i = 0; i < total; i++) {
            Double value = series.get(i);
            if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
                missing++;
            }
        }

        return new MissingStats(total, missing);
    }

    /**
     * 缺失统计。
     */
    private static class MissingStats {
        private final int total;
        private final int missing;

        private MissingStats(int total, int missing) {
            this.total = total;
            this.missing = missing;
        }
    }

    /**
     * 质量门控结果。
     */
    public static class QualityGateResult {
        private final double missingRate;
        private final boolean passed;
        private final SleepStage forcedStage;
        private final String message;

        public QualityGateResult(double missingRate, boolean passed, SleepStage forcedStage, String message) {
            this.missingRate = missingRate;
            this.passed = passed;
            this.forcedStage = forcedStage;
            this.message = message;
        }

        public double getMissingRate() {
            return missingRate;
        }

        public boolean isPassed() {
            return passed;
        }

        public SleepStage getForcedStage() {
            return forcedStage;
        }

        public String getMessage() {
            return message;
        }
    }
}
