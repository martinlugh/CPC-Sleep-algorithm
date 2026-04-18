package com.hrv.executive;

/**
 * 3天个人基线（按同一口径聚合）。
 */
public record BaselineProfile(
        double baselineMeanHrBpm,
        double baselineRmssdMs,
        double baselineSdnnMs,
        double baselineRespirationRateBpm,
        double baselineLfHfRatio
) {
}
