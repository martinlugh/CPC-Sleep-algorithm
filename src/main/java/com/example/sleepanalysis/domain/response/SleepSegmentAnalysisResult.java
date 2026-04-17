package com.example.sleepanalysis.domain.response;

import com.example.sleepanalysis.enums.SleepStage;

/**
 * 单个睡眠片段分析结果。
 */
public class SleepSegmentAnalysisResult {

    /** 片段唯一标识。 */
    private String segmentId;

    /** 预测睡眠阶段。 */
    private SleepStage predictedSleepStage;

    /** 阶段置信度（0~1）。 */
    private Double stageConfidence;

    /** 平均心率（bpm）。 */
    private Double averageHeartRate;

    /** 平均呼吸率（rpm）。 */
    private Double averageRespirationRate;

    /** 平均体动强度。 */
    private Double averageBodyMovement;

    /** 平均血氧（%）。 */
    private Double averageSpo2;

    /** 备注信息。 */
    private String remark;

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public SleepStage getPredictedSleepStage() {
        return predictedSleepStage;
    }

    public void setPredictedSleepStage(SleepStage predictedSleepStage) {
        this.predictedSleepStage = predictedSleepStage;
    }

    public Double getStageConfidence() {
        return stageConfidence;
    }

    public void setStageConfidence(Double stageConfidence) {
        this.stageConfidence = stageConfidence;
    }

    public Double getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(Double averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    public Double getAverageRespirationRate() {
        return averageRespirationRate;
    }

    public void setAverageRespirationRate(Double averageRespirationRate) {
        this.averageRespirationRate = averageRespirationRate;
    }

    public Double getAverageBodyMovement() {
        return averageBodyMovement;
    }

    public void setAverageBodyMovement(Double averageBodyMovement) {
        this.averageBodyMovement = averageBodyMovement;
    }

    public Double getAverageSpo2() {
        return averageSpo2;
    }

    public void setAverageSpo2(Double averageSpo2) {
        this.averageSpo2 = averageSpo2;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
