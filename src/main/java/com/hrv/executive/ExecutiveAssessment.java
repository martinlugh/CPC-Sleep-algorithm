package com.hrv.executive;

/**
 * 统一输出结果。
 */
public record ExecutiveAssessment(
        double respirationRateBpm,
        Level respirationLevel,
        double hrvMs,
        Level hrvLevel,
        double pressureScore,
        Level pressureLevel,
        EmotionLevel emotionLevel,
        double fatigueScore,
        Level fatigueLevel,
        double recoveryScore,
        Level recoveryLevel,
        double depressionScore,
        Level depressionLevel,
        boolean baselineAdjusted,
        String standardsNote
) {
}
