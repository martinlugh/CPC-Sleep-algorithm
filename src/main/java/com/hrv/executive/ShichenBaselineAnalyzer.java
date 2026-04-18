package com.hrv.executive;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 时辰健康判断：
 * - 每个时辰最低 6 组 RRi，少于 6 组为“数据不足”
 * - 与过去 3 天同一时辰基线比较
 * - 无变化/变好：绿色；轻微变差：黄色；严重变差：红色
 */
public final class ShichenBaselineAnalyzer {
    private ShichenBaselineAnalyzer() {
    }

    public static final int MIN_GROUPS_PER_SHICHEN = 6;

    public static Map<Shichen, ShichenHealthJudgement> evaluateAllDay(
            Map<Shichen, List<double[]>> todayData,
            List<Map<Shichen, List<double[]>>> past3DaysData
    ) {
        Map<Shichen, ShichenHealthJudgement> out = new EnumMap<>(Shichen.class);

        for (Shichen s : Shichen.values()) {
            List<double[]> todayGroups = todayData == null ? null : todayData.get(s);
            int todayCount = todayGroups == null ? 0 : todayGroups.size();

            if (todayCount < MIN_GROUPS_PER_SHICHEN) {
                out.put(s, new ShichenHealthJudgement(
                        s,
                        todayCount,
                        Double.NaN,
                        Double.NaN,
                        Double.NaN,
                        PromptColor.GRAY,
                        "数据不足（<6组），无法进行时辰健康判断",
                        false
                ));
                continue;
            }

            double todayScore = averageStateScore(todayGroups);
            double baselineScore = buildShichenBaselineScore(s, past3DaysData);

            if (Double.isNaN(baselineScore)) {
                out.put(s, new ShichenHealthJudgement(
                        s,
                        todayCount,
                        todayScore,
                        Double.NaN,
                        Double.NaN,
                        PromptColor.GRAY,
                        "历史3天该时辰数据不足，无法建立个人基线",
                        true
                ));
                continue;
            }

            double delta = todayScore - baselineScore;
            PromptColor color = classifyColor(todayScore, baselineScore, delta);
            String msg = buildMessage(s, todayScore, baselineScore, delta, color);

            out.put(s, new ShichenHealthJudgement(
                    s,
                    todayCount,
                    todayScore,
                    baselineScore,
                    delta,
                    color,
                    msg,
                    true
            ));
        }

        return out;
    }

    static double buildShichenBaselineScore(Shichen s, List<Map<Shichen, List<double[]>>> past3DaysData) {
        if (past3DaysData == null || past3DaysData.isEmpty()) return Double.NaN;

        List<Double> validDayScores = new ArrayList<>();
        for (Map<Shichen, List<double[]>> day : past3DaysData) {
            if (day == null) continue;
            List<double[]> groups = day.get(s);
            if (groups != null && groups.size() >= MIN_GROUPS_PER_SHICHEN) {
                validDayScores.add(averageStateScore(groups));
            }
        }

        if (validDayScores.isEmpty()) return Double.NaN;
        return validDayScores.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    static double averageStateScore(List<double[]> rriGroups) {
        return rriGroups.stream()
                .mapToDouble(ShichenBaselineAnalyzer::singleGroupStateScore)
                .average()
                .orElse(Double.NaN);
    }

    /**
     * 单组 RRi 的综合健康状态分（越高越好，0~100）。
     * 统一参数口径：使用 ExecutiveAssessment 的 pressure/fatigue/recovery/depression。
     */
    static double singleGroupStateScore(double[] rri) {
        ExecutiveAssessment a = ExecutiveHealthAnalyzer.analyze(rri);
        double positivePressure = 100.0 - a.pressureScore();
        double positiveFatigue = 100.0 - a.fatigueScore();
        double positiveDepression = 100.0 - a.depressionScore();
        double recovery = a.recoveryScore();

        // 加权融合，保持语义一致（恢复越高、负向评分越低 => 状态越好）
        double score = 0.35 * recovery
                + 0.25 * positivePressure
                + 0.20 * positiveFatigue
                + 0.20 * positiveDepression;

        return clamp100(score);
    }

    static PromptColor classifyColor(double todayScore, double baselineScore, double delta) {
        // 规则1：只要优于“健康初始底线”，即使较3天基线变差，也判绿色
        if (todayScore >= JudgementStandards.SHICHEN_INITIAL_HEALTH_SCORE) {
            return PromptColor.GREEN;
        }

        // 规则2：低于健康初始底线时，不健康程度优先于“是否较基线改善”
        double gap = JudgementStandards.SHICHEN_INITIAL_HEALTH_SCORE - todayScore;
        if (gap >= JudgementStandards.SHICHEN_RED_GAP) {
            return PromptColor.RED;
        }
        if (gap >= JudgementStandards.SHICHEN_YELLOW_GAP) {
            return PromptColor.YELLOW;
        }

        // 规则3：接近底线但未达到时，仍参考3天基线变化
        if (delta >= 0) return PromptColor.GREEN;
        if (delta > -10.0) return PromptColor.YELLOW;
        return PromptColor.RED;
    }

    static String buildMessage(Shichen s, double today, double baseline, double delta, PromptColor color) {
        String prefix = s.cnName() + "(" + s.range() + ")";
        return switch (color) {
            case GREEN -> String.format("%s 状态可接受/改善（今日 %.2f vs 基线 %.2f，Δ=%.2f，底线=%.2f）", prefix, today, baseline, delta, JudgementStandards.SHICHEN_INITIAL_HEALTH_SCORE);
            case YELLOW -> String.format("%s 轻度不健康或轻微变差，建议关注（今日 %.2f vs 基线 %.2f，Δ=%.2f，底线=%.2f）", prefix, today, baseline, delta, JudgementStandards.SHICHEN_INITIAL_HEALTH_SCORE);
            case RED -> String.format("%s 明显不健康/显著变差，建议干预复测（今日 %.2f vs 基线 %.2f，Δ=%.2f，底线=%.2f）", prefix, today, baseline, delta, JudgementStandards.SHICHEN_INITIAL_HEALTH_SCORE);
            case GRAY -> prefix + " 数据不足";
        };
    }

    private static double clamp100(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
