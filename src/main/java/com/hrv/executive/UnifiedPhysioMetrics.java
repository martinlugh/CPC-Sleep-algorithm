package com.hrv.executive;

/**
 * 统一参数模型：所有算法输入/中间参数都走该结构，避免参数口径不一致。
 */
public record UnifiedPhysioMetrics(
        double meanHrBpm,
        double rmssdMs,
        double sdnnMs,
        double pnn50Percent,
        double lfHfRatio,
        double respirationRateBpm,
        double meanRriMs
) {
}
