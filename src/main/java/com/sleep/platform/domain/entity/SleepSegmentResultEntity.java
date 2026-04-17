package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sleep_segment_result")
public class SleepSegmentResultEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private LocalDateTime segmentStartTime;
    private BigDecimal alignedStepsInFiveMinutes;
    private BigDecimal motionAlignmentConfidence;
    private Integer rawRriCount;
    private Integer cleanedRriCount;
    private BigDecimal averageHeartRateBpm;
    private BigDecimal hfcPower;
    private BigDecimal lfcPower;
    private BigDecimal vlfcPower;
    private BigDecimal hfcLfcRatio;
    private BigDecimal sd1Ms;
    private BigDecimal sd2Ms;
    private BigDecimal sd2Sd1Ratio;
    private BigDecimal sampleEntropy;
    private String stageBeforeCalibration;
    private String stageAfterCalibration;
    private String smoothedStage;
    private BigDecimal confidenceScore;
    private Boolean qualityPassed;
    private String qualityRemark;
    private String explainTagsJson;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
