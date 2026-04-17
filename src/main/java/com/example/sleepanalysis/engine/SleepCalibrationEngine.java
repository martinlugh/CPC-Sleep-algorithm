package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.enums.SleepStage;
import com.example.sleepanalysis.feature.PoincareAnalysis;

import java.time.OffsetDateTime;

/**
 * 睡眠分期校准引擎。
 */
public class SleepCalibrationEngine {

    /** 低熵阈值。 */
    private static final double LOW_ENTROPY_THRESHOLD = 1.05D;

    /** 高 SD1 阈值（ms）。 */
    private static final double HIGH_SD1_THRESHOLD = 35.0D;

    /** 高 SD2/SD1 阈值。 */
    private static final double HIGH_SD2_SD1_RATIO = 2.2D;

    /**
     * 根据规则进行阶段校准。
     *
     * @param initialDecision 初始分期
     * @param poincareMetrics Poincaré 指标
     * @param sampleEntropy 样本熵
     * @param timestamp 时间戳
     * @return 校准后结果
     */
    public SleepDecisionEngine.SleepDecision calibrate(
            SleepDecisionEngine.SleepDecision initialDecision,
            PoincareAnalysis.PoincareMetrics poincareMetrics,
            double sampleEntropy,
            OffsetDateTime timestamp) {

        SleepStage stage = initialDecision.getStage();
        double confidence = initialDecision.getConfidence();

        // 1. LIGHT -> DEEP（低熵 + 高 SD1）
        if (stage == SleepStage.LIGHT
                && sampleEntropy < LOW_ENTROPY_THRESHOLD
                && poincareMetrics.getSd1() >= HIGH_SD1_THRESHOLD) {
            stage = SleepStage.DEEP;
            confidence = Math.min(0.99D, confidence + 0.12D);
        }

        // 2. LIGHT -> REM（高 SD2/SD1 + 凌晨）
        if (stage == SleepStage.LIGHT
                && poincareMetrics.getSd2Sd1Ratio() >= HIGH_SD2_SD1_RATIO
                && isEarlyMorning(timestamp)) {
            stage = SleepStage.REM;
            confidence = Math.min(0.99D, confidence + 0.10D);
        }

        // 3. REM -> LIGHT（低熵）
        if (stage == SleepStage.REM
                && sampleEntropy < LOW_ENTROPY_THRESHOLD) {
            stage = SleepStage.LIGHT;
            confidence = Math.min(0.99D, confidence + 0.05D);
        }

        // 4. DEEP -> 提升置信度
        if (stage == SleepStage.DEEP) {
            confidence = Math.min(0.99D, confidence + 0.08D);
        }

        return new SleepDecisionEngine.SleepDecision(
                stage,
                confidence,
                initialDecision.getWakeScore(),
                initialDecision.getLightScore(),
                initialDecision.getDeepScore(),
                initialDecision.getRemScore());
    }

    /**
     * 凌晨定义：02:00~05:59。
     */
    private boolean isEarlyMorning(OffsetDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }
        int hour = timestamp.getHour();
        return hour >= 2 && hour <= 5;
    }
}
