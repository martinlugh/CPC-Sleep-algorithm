package com.hrv.executive;

/**
 * 统一判定标准（单位全部统一）：
 * - 心率：bpm
 * - 呼吸率：breaths/min
 * - HRV：ms（使用 RMSSD 主指标）
 * - 评分：0~100（越高越明显）
 */
public final class JudgementStandards {
    private JudgementStandards() {
    }

    // 呼吸率（成人静息）
    public static final double RESP_LOW_MAX = 11.9;
    public static final double RESP_NORMAL_MAX = 20.0;

    // HRV（RMSSD, ms）
    public static final double HRV_GOOD_MIN = 35.0;
    public static final double HRV_BORDERLINE_MIN = 20.0;

    // 通用评分阈值
    public static final double SCORE_HIGH_MIN = 70.0;
    public static final double SCORE_MEDIUM_MIN = 40.0;

    // 恢复评分阈值（越高越好）
    public static final double RECOVERY_HIGH_MIN = 70.0;
    public static final double RECOVERY_MEDIUM_MIN = 40.0;

    // 子午流注时辰判断的健康初始底线（越高越健康）
    public static final double SHICHEN_INITIAL_HEALTH_SCORE = 75.0;
    // 与初始底线差距 >= 15：明显不健康
    public static final double SHICHEN_RED_GAP = 15.0;
    // 与初始底线差距 >= 5 且 < 15：轻度不健康
    public static final double SHICHEN_YELLOW_GAP = 5.0;
}
