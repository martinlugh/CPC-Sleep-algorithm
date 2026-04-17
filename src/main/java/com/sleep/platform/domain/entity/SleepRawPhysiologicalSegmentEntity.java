package com.sleep.platform.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sleep_raw_physiological_segment")
public class SleepRawPhysiologicalSegmentEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private LocalDateTime segmentStartTime;
    private String rriJson;
    private BigDecimal averageHeartRateBpm;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
