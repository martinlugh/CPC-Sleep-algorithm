package com.hrv.executive;

/**
 * 单个时辰的健康判断结果。
 */
public record ShichenHealthJudgement(
        Shichen shichen,
        int dataGroupCount,
        double todayStateScore,
        double baselineStateScore,
        double deltaScore,
        PromptColor promptColor,
        String message,
        boolean enoughData
) {
}
