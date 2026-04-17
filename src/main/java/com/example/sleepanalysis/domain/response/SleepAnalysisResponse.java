package com.example.sleepanalysis.domain.response;

import com.example.sleepanalysis.enums.SleepStage;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 睡眠分析响应对象。
 */
public class SleepAnalysisResponse {

    /** 分析任务唯一标识。 */
    private String analysisId;

    /** 用户唯一标识。 */
    private String userId;

    /** 分析状态。 */
    private String analysisStatus;

    /** 分析完成时间。 */
    private OffsetDateTime analyzedAt;

    /** 分期时间轴。 */
    private List<SleepStageTimelineItem> sleepStageTimeline;

    /** 分段分析结果。 */
    private List<SleepSegmentAnalysisResult> sleepSegmentAnalysisResults;

    /** 主导睡眠阶段。 */
    private SleepStage dominantSleepStage;

    /** 总睡眠时长（分钟）。 */
    private Double totalSleepMinutes;

    /** 深睡时长（分钟）。 */
    private Double deepSleepMinutes;

    /** 快速眼动时长（分钟）。 */
    private Double remSleepMinutes;

    /** 入睡时长（分钟）。 */
    private Double sleepLatencyMinutes;

    /** 觉醒次数。 */
    private Integer awakeningCount;

    /** 睡眠评分（0~100）。 */
    private Integer sleepScore;

    /** 睡眠质量评分（0~100）。 */
    private Integer sleepQualityScore;

    /** 夜间恢复评分（0~100）。 */
    private Integer nightlyRecoveryScore;

    /** 夜间疲劳评分（0~100，越高越疲劳）。 */
    private Integer nightlyFatigueScore;

    /** 入睡时间（第一个稳定睡眠段起点）。 */
    private OffsetDateTime sleepOnsetTime;

    /** 醒来时间（最后持续 WAKE 起点）。 */
    private OffsetDateTime finalWakeTime;

    /** 浅睡时长（分钟）。 */
    private Double lightSleepMinutes;

    /** 清醒时长（分钟）。 */
    private Double awakeMinutes;

    /** 消息说明。 */
    private String message;

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAnalysisStatus() {
        return analysisStatus;
    }

    public void setAnalysisStatus(String analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public OffsetDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(OffsetDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }

    public List<SleepStageTimelineItem> getSleepStageTimeline() {
        return sleepStageTimeline;
    }

    public void setSleepStageTimeline(List<SleepStageTimelineItem> sleepStageTimeline) {
        this.sleepStageTimeline = sleepStageTimeline;
    }

    public List<SleepSegmentAnalysisResult> getSleepSegmentAnalysisResults() {
        return sleepSegmentAnalysisResults;
    }

    public void setSleepSegmentAnalysisResults(List<SleepSegmentAnalysisResult> sleepSegmentAnalysisResults) {
        this.sleepSegmentAnalysisResults = sleepSegmentAnalysisResults;
    }

    public SleepStage getDominantSleepStage() {
        return dominantSleepStage;
    }

    public void setDominantSleepStage(SleepStage dominantSleepStage) {
        this.dominantSleepStage = dominantSleepStage;
    }

    public Double getTotalSleepMinutes() {
        return totalSleepMinutes;
    }

    public void setTotalSleepMinutes(Double totalSleepMinutes) {
        this.totalSleepMinutes = totalSleepMinutes;
    }

    public Double getDeepSleepMinutes() {
        return deepSleepMinutes;
    }

    public void setDeepSleepMinutes(Double deepSleepMinutes) {
        this.deepSleepMinutes = deepSleepMinutes;
    }

    public Double getRemSleepMinutes() {
        return remSleepMinutes;
    }

    public void setRemSleepMinutes(Double remSleepMinutes) {
        this.remSleepMinutes = remSleepMinutes;
    }

    public Double getSleepLatencyMinutes() {
        return sleepLatencyMinutes;
    }

    public void setSleepLatencyMinutes(Double sleepLatencyMinutes) {
        this.sleepLatencyMinutes = sleepLatencyMinutes;
    }

    public Integer getAwakeningCount() {
        return awakeningCount;
    }

    public void setAwakeningCount(Integer awakeningCount) {
        this.awakeningCount = awakeningCount;
    }

    public Integer getSleepScore() {
        return sleepScore;
    }

    public void setSleepScore(Integer sleepScore) {
        this.sleepScore = sleepScore;
    }

    public Integer getSleepQualityScore() {
        return sleepQualityScore;
    }

    public void setSleepQualityScore(Integer sleepQualityScore) {
        this.sleepQualityScore = sleepQualityScore;
    }

    public Integer getNightlyRecoveryScore() {
        return nightlyRecoveryScore;
    }

    public void setNightlyRecoveryScore(Integer nightlyRecoveryScore) {
        this.nightlyRecoveryScore = nightlyRecoveryScore;
    }

    public Integer getNightlyFatigueScore() {
        return nightlyFatigueScore;
    }

    public void setNightlyFatigueScore(Integer nightlyFatigueScore) {
        this.nightlyFatigueScore = nightlyFatigueScore;
    }

    public OffsetDateTime getSleepOnsetTime() {
        return sleepOnsetTime;
    }

    public void setSleepOnsetTime(OffsetDateTime sleepOnsetTime) {
        this.sleepOnsetTime = sleepOnsetTime;
    }

    public OffsetDateTime getFinalWakeTime() {
        return finalWakeTime;
    }

    public void setFinalWakeTime(OffsetDateTime finalWakeTime) {
        this.finalWakeTime = finalWakeTime;
    }

    public Double getLightSleepMinutes() {
        return lightSleepMinutes;
    }

    public void setLightSleepMinutes(Double lightSleepMinutes) {
        this.lightSleepMinutes = lightSleepMinutes;
    }

    public Double getAwakeMinutes() {
        return awakeMinutes;
    }

    public void setAwakeMinutes(Double awakeMinutes) {
        this.awakeMinutes = awakeMinutes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
