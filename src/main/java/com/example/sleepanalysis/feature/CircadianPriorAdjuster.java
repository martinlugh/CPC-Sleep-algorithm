package com.example.sleepanalysis.feature;

import com.example.sleepanalysis.domain.request.UserSleepBaselineProfile;

import java.time.OffsetDateTime;

/**
 * 昼夜节律先验调整器。
 */
public class CircadianPriorAdjuster {

    /**
     * 按时间与个体基线调整阶段先验分数。
     * 分数索引顺序：WAKE, LIGHT, DEEP, REM。
     *
     * @param rawScores 原始分数
     * @param timestamp 当前片段时间
     * @param baselineProfile 用户基线
     * @return 调整并归一化后的分数
     */
    public double[] adjust(double[] rawScores, OffsetDateTime timestamp, UserSleepBaselineProfile baselineProfile) {
        if (rawScores == null || rawScores.length != 4 || timestamp == null) {
            return new double[]{0.25D, 0.25D, 0.25D, 0.25D};
        }

        double wake = Math.max(rawScores[0], 1.0E-6D);
        double light = Math.max(rawScores[1], 1.0E-6D);
        double deep = Math.max(rawScores[2], 1.0E-6D);
        double rem = Math.max(rawScores[3], 1.0E-6D);

        int hour = timestamp.getHour();

        // 基础昼夜先验：前半夜偏深睡，后半夜偏 REM，白天偏清醒。
        if (hour >= 22 || hour <= 1) {
            deep *= 1.25D;
            wake *= 0.85D;
        } else if (hour >= 2 && hour <= 5) {
            rem *= 1.30D;
            deep *= 0.95D;
        } else if (hour >= 6 && hour <= 21) {
            wake *= 1.35D;
            deep *= 0.80D;
            rem *= 0.90D;
        }

        // 基于个体作息基线细调。
        if (baselineProfile != null) {
            int habitualSleepHour = parseHour(baselineProfile.getHabitualSleepStartClock());
            int habitualWakeHour = parseHour(baselineProfile.getHabitualWakeUpClock());

            if (habitualSleepHour >= 0 && isNearHour(hour, habitualSleepHour, 1)) {
                light *= 1.15D;
                wake *= 0.90D;
            }

            if (habitualWakeHour >= 0 && isNearHour(hour, habitualWakeHour, 1)) {
                wake *= 1.20D;
                deep *= 0.85D;
                rem *= 0.90D;
            }
        }

        double sum = wake + light + deep + rem;
        return new double[]{wake / sum, light / sum, deep / sum, rem / sum};
    }

    /**
     * 解析 HH:mm 格式的小时。
     */
    private int parseHour(String hhmm) {
        if (hhmm == null || hhmm.length() < 2) {
            return -1;
        }
        try {
            String hourPart = hhmm.substring(0, 2);
            int h = Integer.parseInt(hourPart);
            if (h < 0 || h > 23) {
                return -1;
            }
            return h;
        } catch (RuntimeException ex) {
            return -1;
        }
    }

    /**
     * 判断小时是否在环形时间轴邻近范围内。
     */
    private boolean isNearHour(int hour, int targetHour, int tolerance) {
        int diff = Math.abs(hour - targetHour);
        int circularDiff = Math.min(diff, 24 - diff);
        return circularDiff <= tolerance;
    }
}
