package com.hrv.executive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hrv.analysis.Classical;

/**
 * 综合健康执行程序：
 * 输入 RRi（ms）-> 输出 呼吸率、HRV、压力、情绪、疲劳、恢复、抑郁风险。
 */
public final class ExecutiveHealthAnalyzer {
    private ExecutiveHealthAnalyzer() {
    }

    /** 无个人基线的默认评估。 */
    public static ExecutiveAssessment analyze(double[] rriMs) {
        UnifiedPhysioMetrics current = calculateMetrics(rriMs);
        return buildAssessment(current, null);
    }

    /**
     * 带 3 天个人基线的评估。
     * baseline3DaysRri 期望长度为 3（不足 3 天也可用可得数据进行平均）。
     */
    public static ExecutiveAssessment analyzeWith3DayBaseline(List<double[]> baseline3DaysRri, double[] todayRri) {
        UnifiedPhysioMetrics current = calculateMetrics(todayRri);
        BaselineProfile baseline = buildBaseline(baseline3DaysRri);
        return buildAssessment(current, baseline);
    }

    static UnifiedPhysioMetrics calculateMetrics(double[] rriMs) {
        Map<String, Double> td = Classical.timeDomain(rriMs);
        Map<String, Double> fd = Classical.frequencyDomain(rriMs, null, 4.0, "cubic", null, null, null);
        double respirationRate = estimateRespirationRate(fd);

        return new UnifiedPhysioMetrics(
                td.get("mhr"),
                td.get("rmssd"),
                td.get("sdnn"),
                td.get("pnn50"),
                fd.get("lf_hf"),
                respirationRate,
                td.get("mrri")
        );
    }

    static BaselineProfile buildBaseline(List<double[]> baseline3DaysRri) {
        if (baseline3DaysRri == null || baseline3DaysRri.isEmpty()) {
            return null;
        }

        List<UnifiedPhysioMetrics> days = new ArrayList<>();
        for (double[] day : baseline3DaysRri) {
            if (day != null && day.length > 2) {
                days.add(calculateMetrics(day));
            }
        }
        if (days.isEmpty()) return null;

        return new BaselineProfile(
                days.stream().mapToDouble(UnifiedPhysioMetrics::meanHrBpm).average().orElse(0),
                days.stream().mapToDouble(UnifiedPhysioMetrics::rmssdMs).average().orElse(0),
                days.stream().mapToDouble(UnifiedPhysioMetrics::sdnnMs).average().orElse(0),
                days.stream().mapToDouble(UnifiedPhysioMetrics::respirationRateBpm).average().orElse(0),
                days.stream().mapToDouble(UnifiedPhysioMetrics::lfHfRatio).average().orElse(0)
        );
    }

    static ExecutiveAssessment buildAssessment(UnifiedPhysioMetrics current, BaselineProfile baseline) {
        Level respLevel = judgeRespiration(current.respirationRateBpm());
        Level hrvLevel = judgeHrv(current.rmssdMs());

        double pressureScore = calculatePressureScore(current, baseline);
        Level pressureLevel = scoreToLevel(pressureScore);

        double recoveryScore = calculateRecoveryScore(current, baseline);
        Level recoveryLevel = recoveryToLevel(recoveryScore);

        EmotionLevel emotion = judgeEmotion(pressureLevel, recoveryLevel, current.rmssdMs());

        double fatigueScore = calculateFatigueScore(current, pressureLevel, recoveryLevel);
        Level fatigueLevel = scoreToLevel(fatigueScore);

        double depressionScore = calculateDepressionScore(current, emotion, fatigueLevel, recoveryLevel);
        Level depressionLevel = scoreToLevel(depressionScore);

        boolean baselineAdjusted = baseline != null;
        String note = "标准统一：RRi(ms)、HR(bpm)、呼吸率(bpm)、RMSSD(ms)、LF/HF 比值；支持3天个人基线校准；结果仅用于健康管理筛查，不替代临床诊断。";

        return new ExecutiveAssessment(
                current.respirationRateBpm(), respLevel,
                current.rmssdMs(), hrvLevel,
                pressureScore, pressureLevel,
                emotion,
                fatigueScore, fatigueLevel,
                recoveryScore, recoveryLevel,
                depressionScore, depressionLevel,
                baselineAdjusted,
                note
        );
    }

    /** 无原始呼吸波形时，用 HF 占比映射近似呼吸率。 */
    static double estimateRespirationRate(Map<String, Double> fd) {
        double hf = fd.get("hf");
        double lf = fd.get("lf");
        double total = Math.max(hf + lf, 1e-6);
        double hfRatio = hf / total;
        return 8.0 + hfRatio * 16.0; // 8~24 次/分
    }

    static Level judgeRespiration(double respBpm) {
        if (respBpm <= JudgementStandards.RESP_LOW_MAX) return Level.LOW;
        if (respBpm <= JudgementStandards.RESP_NORMAL_MAX) return Level.MEDIUM;
        return Level.HIGH;
    }

    static Level judgeHrv(double rmssdMs) {
        if (rmssdMs < JudgementStandards.HRV_BORDERLINE_MIN) return Level.LOW;
        if (rmssdMs < JudgementStandards.HRV_GOOD_MIN) return Level.MEDIUM;
        return Level.HIGH;
    }

    /** 压力分：越高越差。 */
    static double calculatePressureScore(UnifiedPhysioMetrics c, BaselineProfile b) {
        double score = 0;
        score += Math.min(40.0, Math.max(0.0, (c.lfHfRatio() - 1.0) * 20.0));
        score += Math.min(35.0, Math.max(0.0, (35.0 - c.rmssdMs()) * 1.2));
        score += Math.min(25.0, Math.max(0.0, (c.meanHrBpm() - 75.0) * 1.2));

        // 基线修正：若比个人3天基线更差，附加分
        if (b != null) {
            score += Math.min(10.0, Math.max(0.0, (c.meanHrBpm() - b.baselineMeanHrBpm()) * 0.8));
            score += Math.min(10.0, Math.max(0.0, (b.baselineRmssdMs() - c.rmssdMs()) * 0.2));
            score += Math.min(10.0, Math.max(0.0, (c.lfHfRatio() - b.baselineLfHfRatio()) * 8.0));
        }

        return clamp100(score);
    }

    /** 恢复分：越高越好。 */
    static double calculateRecoveryScore(UnifiedPhysioMetrics c, BaselineProfile b) {
        if (b == null) {
            // 无基线时按绝对指标粗略计算
            double score = 50.0;
            score += Math.min(20.0, Math.max(-20.0, (c.rmssdMs() - 35.0) * 0.4));
            score += Math.min(15.0, Math.max(-15.0, (75.0 - c.meanHrBpm()) * 0.5));
            score += Math.min(15.0, Math.max(-15.0, (18.0 - c.respirationRateBpm()) * 1.5));
            return clamp100(score);
        }

        // 基于3天个人基线（个体化）
        double score = 50.0;
        score += Math.min(25.0, Math.max(-25.0, (c.rmssdMs() - b.baselineRmssdMs()) * 0.3));
        score += Math.min(20.0, Math.max(-20.0, (b.baselineMeanHrBpm() - c.meanHrBpm()) * 0.8));
        score += Math.min(15.0, Math.max(-15.0, (b.baselineRespirationRateBpm() - c.respirationRateBpm()) * 2.0));
        score += Math.min(10.0, Math.max(-10.0, (c.sdnnMs() - b.baselineSdnnMs()) * 0.15));
        return clamp100(score);
    }

    static EmotionLevel judgeEmotion(Level pressureLevel, Level recoveryLevel, double rmssd) {
        if (pressureLevel == Level.HIGH || recoveryLevel == Level.LOW || rmssd < 20) return EmotionLevel.NEGATIVE;
        if (pressureLevel == Level.MEDIUM || recoveryLevel == Level.MEDIUM) return EmotionLevel.NEUTRAL;
        return EmotionLevel.POSITIVE;
    }

    static double calculateFatigueScore(UnifiedPhysioMetrics c, Level pressureLevel, Level recoveryLevel) {
        double score = 0;
        score += Math.min(35.0, Math.max(0.0, (40.0 - c.sdnnMs()) * 1.2));
        score += Math.min(25.0, Math.max(0.0, (c.respirationRateBpm() - 18.0) * 3.0));
        if (pressureLevel == Level.HIGH) score += 25.0;
        else if (pressureLevel == Level.MEDIUM) score += 12.0;
        if (recoveryLevel == Level.LOW) score += 15.0;
        else if (recoveryLevel == Level.MEDIUM) score += 8.0;
        return clamp100(score);
    }

    static double calculateDepressionScore(UnifiedPhysioMetrics c, EmotionLevel emotion, Level fatigueLevel, Level recoveryLevel) {
        double score = 0;
        score += Math.min(35.0, Math.max(0.0, (30.0 - c.rmssdMs()) * 1.2));
        if (emotion == EmotionLevel.NEGATIVE) score += 30.0;
        else if (emotion == EmotionLevel.NEUTRAL) score += 15.0;
        if (fatigueLevel == Level.HIGH) score += 20.0;
        else if (fatigueLevel == Level.MEDIUM) score += 10.0;
        if (recoveryLevel == Level.LOW) score += 15.0;
        return clamp100(score);
    }

    static Level scoreToLevel(double score) {
        if (score >= JudgementStandards.SCORE_HIGH_MIN) return Level.HIGH;
        if (score >= JudgementStandards.SCORE_MEDIUM_MIN) return Level.MEDIUM;
        return Level.LOW;
    }

    static Level recoveryToLevel(double score) {
        if (score >= JudgementStandards.RECOVERY_HIGH_MIN) return Level.HIGH;
        if (score >= JudgementStandards.RECOVERY_MEDIUM_MIN) return Level.MEDIUM;
        return Level.LOW;
    }

    private static double clamp100(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
