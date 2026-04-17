package com.example.sleepanalysis.engine;

import com.example.sleepanalysis.domain.response.SleepAnalysisResponse;
import com.example.sleepanalysis.domain.response.SleepSegmentAnalysisResult;
import com.example.sleepanalysis.domain.response.SleepStageTimelineItem;
import com.example.sleepanalysis.enums.SleepStage;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 睡眠汇总统计引擎。
 */
public class SleepSummaryEngine {

    /**
     * 汇总分期结果并写入响应对象。
     *
     * @param response 响应对象
     * @param timeline 平滑后分期时间轴
     * @param segmentResults 片段分析结果
     */
    public void summarize(
            SleepAnalysisResponse response,
            List<SleepStageTimelineItem> timeline,
            List<SleepSegmentAnalysisResult> segmentResults) {

        if (response == null || timeline == null || timeline.isEmpty()) {
            return;
        }

        double awakeMinutes = 0.0D;
        double lightMinutes = 0.0D;
        double deepMinutes = 0.0D;
        double remMinutes = 0.0D;

        OffsetDateTime sleepOnsetTime = null;
        OffsetDateTime finalWakeTime = null;

        int awakeningCount = 0;
        SleepStage prevStage = null;

        for (int i = 0; i < timeline.size(); i++) {
            SleepStageTimelineItem item = timeline.get(i);
            double minutes = minutesBetween(item.getStageStartTime(), item.getStageEndTime());
            SleepStage stage = item.getSleepStage();

            if (stage == SleepStage.WAKE) {
                awakeMinutes += minutes;
            } else if (stage == SleepStage.LIGHT) {
                lightMinutes += minutes;
            } else if (stage == SleepStage.DEEP) {
                deepMinutes += minutes;
            } else if (stage == SleepStage.REM) {
                remMinutes += minutes;
            }

            if (sleepOnsetTime == null && isStableSleep(timeline, i)) {
                sleepOnsetTime = item.getStageStartTime();
            }

            if (prevStage != null && prevStage != SleepStage.WAKE && stage == SleepStage.WAKE) {
                awakeningCount++;
            }
            prevStage = stage;
        }

        // 醒来时间：最后持续 WAKE（至少2段）
        for (int i = timeline.size() - 2; i >= 0; i--) {
            SleepStage s1 = timeline.get(i).getSleepStage();
            SleepStage s2 = timeline.get(i + 1).getSleepStage();
            if (s1 == SleepStage.WAKE && s2 == SleepStage.WAKE) {
                finalWakeTime = timeline.get(i).getStageStartTime();
                break;
            }
        }

        double totalSleepMinutes = lightMinutes + deepMinutes + remMinutes;

        response.setTotalSleepMinutes(totalSleepMinutes);
        response.setDeepSleepMinutes(deepMinutes);
        response.setRemSleepMinutes(remMinutes);
        response.setAwakeningCount(awakeningCount);

        double latencyMinutes = 0.0D;
        if (sleepOnsetTime != null && timeline.get(0).getStageStartTime() != null) {
            latencyMinutes = minutesBetween(timeline.get(0).getStageStartTime(), sleepOnsetTime);
        }
        response.setSleepLatencyMinutes(latencyMinutes);

        response.setDominantSleepStage(resolveDominantStage(lightMinutes, deepMinutes, remMinutes, awakeMinutes));

        int sleepQualityScore = computeSleepQualityScore(totalSleepMinutes, deepMinutes, remMinutes, awakeMinutes, latencyMinutes);
        int nightlyRecoveryScore = computeRecoveryScore(deepMinutes, remMinutes, totalSleepMinutes);
        int nightlyFatigueScore = computeFatigueScore(sleepQualityScore, awakeMinutes, awakeningCount);

        response.setSleepScore(sleepQualityScore);
        response.setSleepQualityScore(sleepQualityScore);
        response.setNightlyRecoveryScore(nightlyRecoveryScore);
        response.setNightlyFatigueScore(nightlyFatigueScore);
        response.setSleepOnsetTime(sleepOnsetTime);
        response.setFinalWakeTime(finalWakeTime);
        response.setLightSleepMinutes(lightMinutes);
        response.setAwakeMinutes(awakeMinutes);

        if (segmentResults != null && !segmentResults.isEmpty()) {
            response.setMessage("分析完成，已输出分期时间轴与睡眠评分。");
        }
    }

    /**
     * 入睡定义：连续两段均非 WAKE 且非 UNKNOWN。
     */
    private boolean isStableSleep(List<SleepStageTimelineItem> timeline, int index) {
        if (index + 1 >= timeline.size()) {
            return false;
        }
        SleepStage current = timeline.get(index).getSleepStage();
        SleepStage next = timeline.get(index + 1).getSleepStage();
        return isSleepStage(current) && isSleepStage(next);
    }

    /**
     * 睡眠阶段判断。
     */
    private boolean isSleepStage(SleepStage stage) {
        return stage == SleepStage.LIGHT || stage == SleepStage.DEEP || stage == SleepStage.REM;
    }

    /**
     * 计算分钟差。
     */
    private double minutesBetween(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0.0D;
        }
        long seconds = Duration.between(start, end).getSeconds();
        return seconds / 60.0D;
    }

    /**
     * 计算主导分期。
     */
    private SleepStage resolveDominantStage(double light, double deep, double rem, double awake) {
        double max = light;
        SleepStage stage = SleepStage.LIGHT;
        if (deep > max) {
            max = deep;
            stage = SleepStage.DEEP;
        }
        if (rem > max) {
            max = rem;
            stage = SleepStage.REM;
        }
        if (awake > max) {
            stage = SleepStage.WAKE;
        }
        return stage;
    }

    /**
     * 睡眠质量评分（0-100）。
     */
    private int computeSleepQualityScore(double totalSleepMinutes, double deepMinutes, double remMinutes, double awakeMinutes, double latencyMinutes) {
        double base = 100.0D;

        if (totalSleepMinutes < 360.0D) {
            base -= (360.0D - totalSleepMinutes) * 0.08D;
        }

        double restorativeRatio = totalSleepMinutes <= 1.0E-9D ? 0.0D : (deepMinutes + remMinutes) / totalSleepMinutes;
        if (restorativeRatio < 0.35D) {
            base -= (0.35D - restorativeRatio) * 120.0D;
        }

        base -= Math.min(awakeMinutes * 0.4D, 20.0D);
        base -= Math.min(latencyMinutes * 0.3D, 15.0D);

        return clampScore(base);
    }

    /**
     * 恢复评分（0-100）。
     */
    private int computeRecoveryScore(double deepMinutes, double remMinutes, double totalSleepMinutes) {
        if (totalSleepMinutes <= 0.0D) {
            return 0;
        }

        double deepRatio = deepMinutes / totalSleepMinutes;
        double remRatio = remMinutes / totalSleepMinutes;
        double score = deepRatio * 65.0D + remRatio * 35.0D;
        return clampScore(score * 2.0D);
    }

    /**
     * 疲劳评分（0-100，越高越疲劳）。
     */
    private int computeFatigueScore(int sleepQualityScore, double awakeMinutes, int awakeningCount) {
        double score = 100.0D - sleepQualityScore;
        score += Math.min(awakeMinutes * 0.25D, 20.0D);
        score += Math.min(awakeningCount * 3.0D, 15.0D);
        return clampScore(score);
    }

    /**
     * 限幅到整数分值区间。
     */
    private int clampScore(double score) {
        if (score < 0.0D) {
            return 0;
        }
        if (score > 100.0D) {
            return 100;
        }
        return (int) Math.round(score);
    }
}
