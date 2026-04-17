package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sleep_analysis_result")
public class SleepAnalysisResultEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private LocalDateTime mainSleepStartTime;
    private LocalDateTime mainSleepWakeUpTime;
    private Integer mainSleepLatencyMinutes;
    private Integer mainSleepTotalMinutes;
    private Integer mainSleepDeepMinutes;
    private Integer mainSleepLightMinutes;
    private Integer mainSleepRemMinutes;
    private Integer mainSleepAwakeMinutes;
    private BigDecimal mainSleepQualityScore;
    private BigDecimal nightlyRecoveryScore;
    private BigDecimal nightlyFatigueScore;
    private BigDecimal dataQualityScore;
    private BigDecimal mainSleepEfficiency;
    private Integer mainSleepAwakenCount;
    private Integer dailyTotalSleepMinutes;
    private Integer daytimeNapTotalMinutes;
    private Integer daytimeNapCount;
    private String scoreExplanationJson;
    private String sleepOnsetReasonJson;
    private String wakeUpReasonJson;
    private String daytimeNapSummaryJson;
    private String alignmentExplanationJson;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
