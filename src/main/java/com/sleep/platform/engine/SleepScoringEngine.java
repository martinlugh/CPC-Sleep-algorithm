package com.sleep.platform.engine;

import com.sleep.platform.domain.model.DailySleepAggregationResult;
import com.sleep.platform.domain.model.MainSleepSummaryResult;
import com.sleep.platform.domain.model.SleepScoreResult;
import com.sleep.platform.domain.response.DaytimeNapResultItem;
import com.sleep.platform.domain.response.SleepSegmentAnalysisResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SleepScoringEngine {

    public SleepScoreResult score(MainSleepSummaryResult summary,
                                  DailySleepAggregationResult dailyAggregationResult,
                                  List<SleepSegmentAnalysisResult> segmentResultList) {
        SleepScoreResult scoreResult = new SleepScoreResult();
        double structureScore = calculateStructureScore(summary);
        double efficiencyScore = (summary.getMainSleepEfficiency() == null ? 0.0 : summary.getMainSleepEfficiency()) * 100.0;
        double awakenPenalty = Math.min(20.0, (summary.getMainSleepAwakenCount() == null ? 0 : summary.getMainSleepAwakenCount()) * 3.0);

        double mainQuality = clamp(0.0, 100.0, structureScore * 0.55 + efficiencyScore * 0.45 - awakenPenalty);
        double nightlyRecovery = clamp(0.0, 100.0, structureScore * 0.6 + efficiencyScore * 0.4);
        double fatigue = clamp(0.0, 100.0, 100.0 - nightlyRecovery + awakenPenalty * 0.4);
        double dataQuality = calculateDataQuality(segmentResultList);

        scoreResult.setMainSleepQualityScore(mainQuality);
        scoreResult.setNightlyRecoveryScore(nightlyRecovery);
        scoreResult.setNightlyFatigueScore(fatigue);
        scoreResult.setDataQualityScore(dataQuality);
        scoreResult.setScoreExplanation(buildScoreExplanation(summary, dailyAggregationResult, dataQuality,
                mainQuality, nightlyRecovery, fatigue));
        return scoreResult;
    }

    private double calculateStructureScore(MainSleepSummaryResult summary) {
        int total = Math.max(1, summary.getMainSleepTotalMinutes() == null ? 0 : summary.getMainSleepTotalMinutes());
        double deepRatio = (summary.getMainSleepDeepMinutes() == null ? 0 : summary.getMainSleepDeepMinutes()) * 1.0 / total;
        double remRatio = (summary.getMainSleepRemMinutes() == null ? 0 : summary.getMainSleepRemMinutes()) * 1.0 / total;
        double lightRatio = (summary.getMainSleepLightMinutes() == null ? 0 : summary.getMainSleepLightMinutes()) * 1.0 / total;
        return clamp(0.0, 100.0, 100.0 - Math.abs(deepRatio - 0.22) * 120.0 - Math.abs(remRatio - 0.20) * 100.0 - Math.abs(lightRatio - 0.58) * 60.0);
    }

    private double calculateDataQuality(List<SleepSegmentAnalysisResult> segmentResultList) {
        if (segmentResultList == null || segmentResultList.isEmpty()) {
            return 0.0;
        }
        int passCount = 0;
        int lowAlignmentCount = 0;
        for (SleepSegmentAnalysisResult result : segmentResultList) {
            if (Boolean.TRUE.equals(result.getQualityPassed())) {
                passCount++;
            }
            if (result.getQualityRemark() != null && result.getQualityRemark().contains("运动对齐置信度=0.")) {
                lowAlignmentCount++;
            }
        }
        double quality = passCount * 100.0 / segmentResultList.size();
        if (lowAlignmentCount > segmentResultList.size() / 2) {
            quality = quality * 0.95;
        }
        return clamp(0.0, 100.0, quality);
    }

    private Map<String, Object> buildScoreExplanation(MainSleepSummaryResult summary,
                                                      DailySleepAggregationResult dailyAggregationResult,
                                                      double dataQuality,
                                                      double mainQuality,
                                                      double recovery,
                                                      double fatigue) {
        Map<String, Object> explanation = new HashMap<>();
        explanation.put("mainSleepQualityScore", mainQuality);
        explanation.put("nightlyRecoveryScore", recovery);
        explanation.put("nightlyFatigueScore", fatigue);
        explanation.put("dataQualityScore", dataQuality);
        explanation.put("mainSleepStructure", Map.of(
                "deepMinutes", summary.getMainSleepDeepMinutes(),
                "lightMinutes", summary.getMainSleepLightMinutes(),
                "remMinutes", summary.getMainSleepRemMinutes(),
                "awakeMinutes", summary.getMainSleepAwakeMinutes()
        ));
        explanation.put("dailySleepReference", Map.of(
                "dailyTotalSleepMinutes", dailyAggregationResult.getDailyTotalSleepMinutes(),
                "daytimeNapTotalMinutes", dailyAggregationResult.getDaytimeNapTotalMinutes(),
                "daytimeNapCount", dailyAggregationResult.getDaytimeNapCount()
        ));

        int napCount = dailyAggregationResult.getDaytimeNapCount() == null ? 0 : dailyAggregationResult.getDaytimeNapCount();
        if (napCount > 0) {
            explanation.put("napQualityScore", calculateNapQuality(dailyAggregationResult.getNapResultList()));
        }
        explanation.put("motionEvidencePolicy", "当运动时间对齐置信度较低时，运动评分项自动降权，最终分数以生理主证据为主。");
        return explanation;
    }

    private double calculateNapQuality(List<DaytimeNapResultItem> napResultList) {
        if (napResultList == null || napResultList.isEmpty()) {
            return 0.0;
        }
        double score = 0.0;
        for (DaytimeNapResultItem item : napResultList) {
            int minutes = item.getNapTotalMinutes() == null ? 0 : item.getNapTotalMinutes();
            double segmentScore = 100.0 - Math.abs(minutes - 30) * 1.2;
            score += clamp(0.0, 100.0, segmentScore);
        }
        return score / napResultList.size();
    }

    private double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }
}
