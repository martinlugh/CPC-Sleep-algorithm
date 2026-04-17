package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("sleep_user_baseline")
public class SleepUserBaselineEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private BigDecimal restingHeartRateBpm;
    private LocalTime typicalSleepStartTime;
    private LocalTime typicalWakeTime;
    private BigDecimal baselineHrvSdnnMs;
    private Integer baselineSleepLatencyMinutes;
    private BigDecimal baselineSleepEfficiency;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
